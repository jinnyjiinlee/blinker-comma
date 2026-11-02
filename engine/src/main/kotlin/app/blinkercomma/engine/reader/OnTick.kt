package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.output.Transition
import kotlin.time.Duration

internal fun onTick(state: ReaderState, at: Duration, params: CommaCardParams): Transition<ReaderState> =
    when (val phase = state.phase) {
        is ReaderPhase.Reading -> holdIfOverdue(state, phase.armedAt, at, params)
        is ReaderPhase.Holding -> phase.offSince?.takeIf { params.isAutoResume }
            ?.let { resumeWhenHeld(state, it, at, params) }
            ?: Transition(state)
        ReaderPhase.Stopped, ReaderPhase.FocusPaused -> Transition(state)
    }

/** 예약 뒤 상한 안에 문장이 안 끝나면 그 자리에서 멈춘다(R4) — 재개는 같은 문장 처음부터. */
private fun holdIfOverdue(
    state: ReaderState,
    armedAt: Duration?,
    at: Duration,
    params: CommaCardParams,
): Transition<ReaderState> {
    val waited = armedAt?.let { at - it }
    return if (waited != null && waited >= params.endWaitLimit) {
        hold(state, HoldReason.MID_SENTENCE, ReaderLog.HeldMid(state.index, waited))
    } else {
        Transition(state)
    }
}
