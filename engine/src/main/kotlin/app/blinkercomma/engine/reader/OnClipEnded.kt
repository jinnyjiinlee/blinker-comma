package app.blinkercomma.engine.reader

import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition

/** 문장 하나가 끝났다. 쉼표가 예약돼 있으면 다음 문장을 시작하지 않고 멈춘다(R1). */
internal fun onClipEnded(state: ReaderState, event: ReaderEvent.ClipEnded): Transition<ReaderState> {
    val phase = state.phase
    if (phase !is ReaderPhase.Reading || event.index != state.index) return Transition(state)
    val done = state.copy(tally = state.tally.addSentence())
    val next = state.index + 1
    return when {
        next >= state.clipCount -> finish(done)
        phase.armedAt != null -> hold(
            done.copy(index = next),
            HoldReason.SENTENCE_END,
            ReaderLog.HeldAtEnd(state.index),
        )
        else -> readNext(done.copy(index = next))
    }
}

/** 쉼표 없이 다음 문장으로 — 문장 사이엔 틈을 두지 않는다. */
private fun readNext(state: ReaderState): Transition<ReaderState> = Transition(
    state,
    listOf(
        Effect.PlayClip(state.index, isFromStart = true),
        toWriteLog(ReaderLog.ClipStarted(state.index)),
        Effect.SaveTally(state.tally),
    ),
)

/** 팩 끝 — 멈출 다음 문장이 없으니 쉼표 없이 정지하고 처음으로 되감는다. */
private fun finish(state: ReaderState): Transition<ReaderState> = Transition(
    state.copy(phase = ReaderPhase.Stopped, index = 0),
    listOf(toWriteLog(ReaderLog.PackFinished), Effect.SaveTally(state.tally)),
)
