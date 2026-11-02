package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.comma.CommaSnapshot
import app.blinkercomma.engine.output.Transition
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import app.blinkercomma.engine.tally.ReadingTally
import kotlin.time.Duration

data class ReaderState(
    /** 팩의 문장(클립) 수. */
    val clipCount: Int,
    val phase: ReaderPhase = ReaderPhase.Stopped,
    /** 재생 중이거나 다음에 재생할 문장(0부터). 로그엔 1부터 — 팩 파일 번호 `s01`과 같게. */
    val index: Int = 0,
    val lever: TurnSignal = TurnSignal.OFF,
    /** null = 확인 안 됨(아직 콜백 없음). */
    val gear: Gear? = null,
    /** null = 확인 안 됨(권한 거부·미수신) — 이때는 속도 예외 없이 레버 + D로 판정(원칙 4). */
    val speedMps: Double? = null,
    /** 이번 레버 ON 구간에 이미 쉼표를 냈다(R6). 레버가 내려오면 풀린다. */
    val isCommaUsed: Boolean = false,
    val tally: ReadingTally = ReadingTally(),
) {
    val snapshot: CommaSnapshot get() = CommaSnapshot(lever, gear, speedMps)

    /** 앱이 다음 [ReaderEvent.Tick]을 보낼 시각. null = 시간이 흘러도 바뀔 것 없음. */
    fun nextDeadline(params: CommaCardParams = CommaCardParams.DEFAULT): Duration? = when (val phase = phase) {
        is ReaderPhase.Reading -> phase.armedAt?.plus(params.endWaitLimit)
        is ReaderPhase.Holding -> phase.offSince?.plus(params.offDebounce)?.takeIf { params.isAutoResume }
        ReaderPhase.Stopped, ReaderPhase.FocusPaused -> null
    }
}

/**
 * 깜빡이 쉼표 엔진 입구. 레버를 올린 채 문장 끝을 지나면 거기서 멈추고, 레버가 [CommaCardParams.offDebounce]
 * 동안 내려가 있으면 다음 문장 — 규칙 번호(R1~R10)는 설계서 「쉼표 규칙」 표.
 */
fun reduce(
    state: ReaderState,
    event: ReaderEvent,
    params: CommaCardParams = CommaCardParams.DEFAULT,
): Transition<ReaderState> = when (event) {
    is ReaderEvent.Car -> onCarEvent(state, event.event, params)
    is ReaderEvent.ClipEnded -> onClipEnded(state, event)
    is ReaderEvent.PlayPressed -> onPlayPressed(state, event, params)
    is ReaderEvent.PausePressed -> onPausePressed(state, event)
    is ReaderEvent.SkipPressed -> onSkipPressed(state, event, params)
    is ReaderEvent.FocusChanged -> onFocusChanged(state, event, params)
    is ReaderEvent.Tick -> onTick(state, event.at, params)
}
