package app.blinkercomma.player

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import app.blinkercomma.data.LoadedPack
import app.blinkercomma.engine.pack.SentenceClip
import app.blinkercomma.engine.reader.ReaderEvent
import kotlin.time.Duration

/**
 * 클립(= 문장 하나) 재생기. 재생마다 새 MediaPlayer를 열고 멈춤·끝에선 놓는다(이어 듣기는 새로 열어 멈춘 자리로).
 * 문장 끝 = 재생 시간이 팩의 클립 길이(`durationMs`)에 닿은 순간(단조 시계). 완료 콜백은 기록만 한다 — 에뮬레이터
 * `-no-audio`에서 콜백이 늦게도(4.8초/4.0초 클립), 이르게도(2.36초/3.0초 클립) 왔다(D2 실측). 실차에선 둘이 거의 같다.
 * MediaPlayer 호출은 전부 전용 스레드([ClipDeck])에서 — 에뮬레이터 `-no-audio`에서 열기가 2~3.5초 막힌 적이 있어
 * 메인(엔진) 스레드를 막지 않게(D2 실측 — docs/signals_measured.md). 이 클래스의 공개 함수는 메인 스레드에서.
 */
class ClipPlayer(
    private val context: Context,
    private val pack: LoadedPack,
    private val onEvent: (ReaderEvent) -> Unit,
    private val onSoundStarted: () -> Unit,
    private val now: () -> Duration,
) {
    private val main = Handler(Looper.getMainLooper())
    private val worker = HandlerThread("clip-player").apply { start() }
    private val deck = ClipDeck(context, Handler(worker.looper))
    private val clipClock = Runnable { onFinished() }

    /** 재생 요청마다 1 증가 — 늦게 온 「시작됨」·완료 콜백이 지금 요청 것인지 가린다. */
    private var ticket = 0
    private var requested: PausedClip? = null
    private var playing: PlayingClip? = null
    private var paused: PausedClip? = null

    // 첫 ▶도 준비를 기다리지 않게 — 준비만, 소리는 안 냄(자동 재생 금지와 무관)
    init {
        preload(0)
    }

    /** `isFromStart = false`면 같은 문장의 멈춘 자리부터(포커스 복귀). 쉼표 뒤 재개는 엔진이 늘 처음부터를 준다. */
    fun play(index: Int, isFromStart: Boolean) {
        val ready = pack as? LoadedPack.Ready ?: return
        val clip = ready.pack.clips[index]
        val fromMs = paused?.takeIf { !isFromStart && it.index == index }?.positionMs ?: 0L
        val mine = settle()
        requested = PausedClip(index, fromMs)
        val askedAt = now()
        deck.open(ClipRequest(mine, "${ready.folder}/${clip.file}", fromMs), onStarted = { startedAt ->
            main.post { onStarted(mine, PlayingClip(index, fromMs, startedAt), clip, startedAt - askedAt) }
        }, onCompleted = { main.post { if (mine == ticket) logCallback() } })
    }

    fun pause() {
        val position = playing?.let { PausedClip(it.index, it.positionMs(now(), clipOf(it.index))) } ?: requested
        settle()
        paused = position
        deck.close()
        position?.let { preload(it.index) }
        Log.i(TAG, "pause s=${position?.index?.plus(1)} atMs=${position?.positionMs}")
    }

    fun release() {
        settle()
        deck.shutdown()
        worker.quitSafely()
    }

    private fun onStarted(mine: Int, clip: PlayingClip, sentence: SentenceClip, openTook: Duration) {
        if (mine != ticket) return
        requested = null
        playing = clip
        main.postDelayed(clipClock, sentence.durationMs - clip.fromMs)
        onSoundStarted()
        preload(clip.index + 1)
        val openMs = openTook.inWholeMilliseconds
        Log.i(TAG, "start s=${clip.index + 1} fromMs=${clip.fromMs} clipMs=${sentence.durationMs} openMs=$openMs")
    }

    private fun onFinished() {
        val clip = playing ?: return
        val playedMs = (now() - clip.startedAt).inWholeMilliseconds
        settle()
        deck.finish()
        Log.i(TAG, "end s=${clip.index + 1} by=clock playedMs=$playedMs")
        onEvent(ReaderEvent.ClipEnded(now(), clip.index))
    }

    // 실차에서 콜백과 시계가 얼마나 어긋나는지 재려고 남긴다(에뮬레이터 값은 믿지 않음)
    private fun logCallback() {
        val clip = playing ?: return
        Log.i(TAG, "completion callback s=${clip.index + 1} playedMs=${(now() - clip.startedAt).inWholeMilliseconds}")
    }

    /** 지금 요청·재생을 무효로 하고 새 표를 뗀다. */
    private fun settle(): Int {
        main.removeCallbacks(clipClock)
        playing = null
        requested = null
        paused = null
        return ++ticket
    }

    /** [index]부터 [LOOKAHEAD]개를 미리 준비 — 문장 사이·쉼표 뒤 재개가 준비 시간을 기다리지 않게. */
    private fun preload(index: Int) {
        val ready = pack as? LoadedPack.Ready ?: return
        val clips = ready.pack.clips.drop(index).take(LOOKAHEAD)
        deck.preload(clips.map { "${ready.folder}/${it.file}" })
    }

    private fun clipOf(index: Int): SentenceClip? = (pack as? LoadedPack.Ready)?.pack?.clips?.getOrNull(index)

    private companion object {
        const val TAG = "CommaPlayer"
        const val LOOKAHEAD = 2 // 준비가 한 번 막혀도(에뮬레이터 최대 7.6초 — D4) 바로 다음 문장은 준비돼 있게
    }
}
