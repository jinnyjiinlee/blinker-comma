package app.blinkercomma.engine.support

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.pack.PackFile
import app.blinkercomma.engine.pack.toPackFile
import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.reader.reduce
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import app.blinkercomma.engine.tally.ReadingTally
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val VC_SPEED_PER_MPS = 3.6 // VC 속도 칸은 km/h 정수
private val TAIL = 3.seconds // 마지막 줄 뒤 디바운스(0.8초)가 끝날 만큼
private val ANCHOR_LIMIT = 60.seconds

/** 시나리오 테스트용 합성 팩의 문장 길이. */
fun loadStageClips(): List<Duration> {
    val text = requireNotNull(object {}.javaClass.getResource("/stage_pack.json")).readText()
    return (toPackFile(text) as PackFile.Parsed).pack.clips.map { it.duration }
}

/**
 * 앱(서비스 + 플레이어)을 흉내 내며 대본을 돌린다. 효과대로 클립을 재생·멈추고, 클립이 끝나면 ClipEnded,
 * [ReaderState.nextDeadline]엔 Tick — 앱이 할 일과 같다. 신호는 대본 줄에서만 온다(모의 0).
 */
class StageRun(
    private val clips: List<Duration> = loadStageClips(),
    private var params: CommaCardParams = CommaCardParams.DEFAULT,
) {
    private var state = ReaderState(clipCount = clips.size)
    private var now = Duration.ZERO
    private var anchorClip = 0
    private var playing = 0
    private var endsAt: Duration? = null
    private var remaining = Duration.ZERO
    private val logs = mutableListOf<TimedLine>()
    private val glyphs = mutableListOf<Pair<Duration, Boolean>>()
    private val tallies = mutableListOf<Pair<Duration, ReadingTally>>()
    private val anchors = mutableListOf<Duration>()

    fun run(lines: List<StageLine>): StageResult {
        val missing = lines.firstOrNull { !step(it) }
        if (missing == null) advanceTo(now + TAIL)
        return StageResult(logs.toList(), glyphs.toList(), tallies.toList(), anchors.toList(), missing?.value, state)
    }

    private fun step(line: StageLine): Boolean {
        val base = anchors.lastOrNull() ?: Duration.ZERO
        when (val time = line.time) {
            StageTime.Anchor -> return awaitLog(line.value)
            is StageTime.AfterAnchor -> advanceTo(base + time.offset)
            is StageTime.BeforeEnd -> advanceTo(base + clips[anchorClip] - time.offset)
        }
        apply(line)
        return true
    }

    /** anchor: 이 줄부터 새로 찍히는 로그에 글자가 나올 때까지 시간을 흘린다. 문장 번호가 있으면 기억(`end-X`용). */
    private fun awaitLog(text: String): Boolean {
        val from = logs.size
        val limit = now + ANCHOR_LIMIT
        do {
            val isWaiting = logs.drop(from).none { text in it.line } && fireNext { it <= limit }
        } while (isWaiting)
        val hit = logs.drop(from).firstOrNull { text in it.line } ?: return false
        anchors += hit.at
        Regex("""s=(\d+)""").find(hit.line)?.let { anchorClip = it.groupValues[1].toInt() - 1 }
        return true
    }

    private fun advanceTo(target: Duration) {
        require(target >= now) { "대본 시각이 거꾸로: $target < $now" }
        do {
            val isFired = fireNext { it < target }
        } while (isFired)
        now = target
    }

    /** 다음 내부 사건(클립 끝·마감 Tick)이 조건 안이면 그 시각으로 가서 넣는다. */
    private fun fireNext(isWithin: (Duration) -> Boolean): Boolean {
        val (at, event) = nextInternal()?.takeIf { isWithin(it.first) } ?: return false
        now = at
        feed(event)
        return true
    }

    private fun nextInternal(): Pair<Duration, ReaderEvent>? {
        val clipEnd = endsAt?.let { it to ReaderEvent.ClipEnded(it, playing) }
        val deadline = state.nextDeadline(params)?.let { maxOf(it, now) }?.let { it to ReaderEvent.Tick(it) }
        return listOfNotNull(clipEnd, deadline).minByOrNull { it.first }
    }

    private fun apply(line: StageLine) {
        val value = line.value
        when (line.signal) {
            "turn_signal" -> feed(ReaderEvent.Car(CarEvent.TurnSignalChanged(now, TurnSignal.valueOf(value))))
            "gear" -> feed(ReaderEvent.Car(CarEvent.GearChanged(now, Gear.valueOf(value))))
            "speed" -> feed(ReaderEvent.Car(CarEvent.SpeedChanged(now, value.toDouble() / VC_SPEED_PER_MPS)))
            "play" -> feed(ReaderEvent.PlayPressed(now, PressSource.valueOf(value)))
            "auto_resume" -> params = params.copy(isAutoResume = value == "ON")
            "media_key" -> feed(toMediaSessionPress())
            "focus" -> feed(ReaderEvent.FocusChanged(now, AudioFocus.valueOf(value)))
            else -> error("모르는 신호: ${line.signal}")
        }
    }

    /** MediaSessionCompat 기본 처리: PLAY_PAUSE 키는 재생 중이면 onPause, 아니면 onPlay. */
    private fun toMediaSessionPress(): ReaderEvent = if (state.phase is ReaderPhase.Reading) {
        ReaderEvent.PausePressed(now, PressSource.MEDIA_KEY)
    } else {
        ReaderEvent.PlayPressed(now, PressSource.MEDIA_KEY)
    }

    private fun feed(event: ReaderEvent) {
        if (event is ReaderEvent.ClipEnded) endsAt = null
        val transition = reduce(state, event, params)
        state = transition.state
        transition.effects.forEach(::perform)
    }

    private fun perform(effect: Effect) {
        when (effect) {
            is Effect.PlayClip -> startClip(effect)
            Effect.PauseClip -> {
                remaining = (endsAt ?: now) - now
                endsAt = null
            }
            is Effect.ShowHoldGlyph -> glyphs += now to effect.isOn
            is Effect.WriteLog -> logs += TimedLine(now, effect.line)
            is Effect.SaveTally -> tallies += now to effect.tally
        }
    }

    /** 이어 재생이면 멈춘 자리부터 남은 길이만, 아니면 문장 전체 길이. */
    private fun startClip(effect: Effect.PlayClip) {
        val isContinue = !effect.isFromStart && effect.index == playing
        endsAt = now + if (isContinue) remaining else clips[effect.index]
        playing = effect.index
    }
}
