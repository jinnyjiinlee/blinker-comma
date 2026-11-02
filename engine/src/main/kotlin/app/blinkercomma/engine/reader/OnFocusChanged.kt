package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition

/**
 * 표준 포커스 매너(R10). 쉼표 수단으로 쓰지 않는다(원칙 7) — 남이 가져가면 멈추고 돌려주면 이어 듣는다.
 * 쉼표 중엔 포커스를 돌려받아도 재개하지 않는다(레버·▶로만 풀림).
 */
internal fun onFocusChanged(
    state: ReaderState,
    event: ReaderEvent.FocusChanged,
    params: CommaCardParams,
): Transition<ReaderState> {
    val seen = ReaderLog.FocusSeen(event.focus)
    val phase = state.phase
    return when {
        event.focus == AudioFocus.LOSS && phase != ReaderPhase.Stopped -> stop(state, seen)
        event.focus == AudioFocus.LOSS_TRANSIENT && phase is ReaderPhase.Reading ->
            Transition(state.copy(phase = ReaderPhase.FocusPaused), listOf(Effect.PauseClip, toWriteLog(seen)))
        event.focus == AudioFocus.GAIN && phase == ReaderPhase.FocusPaused ->
            Transition(state, listOf(toWriteLog(seen))).then { startReading(it, event.at, params, isFromStart = false) }
        else -> Transition(state, listOf(toWriteLog(seen)))
    }
}
