package app.blinkercomma.engine.reader

import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition
import app.blinkercomma.engine.signal.Gear

/** 문장 하나가 끝났다. 쉼표가 예약돼 있으면 팩 끝이어도 먼저 멈춘다(R1) — 재개할 다음 문장이 없으면 처음(0)으로. */
internal fun onClipEnded(state: ReaderState, event: ReaderEvent.ClipEnded): Transition<ReaderState> {
    val phase = state.phase
    if (phase !is ReaderPhase.Reading || event.index != state.index) return Transition(state)
    val done = state.copy(tally = state.tally.addSentence())
    val next = state.index + 1
    val isLastClip = next >= state.clipCount
    val resumeIndex = if (isLastClip) 0 else next
    return when {
        phase.armedAt != null -> holdAtEnd(done, resumeIndex, state.index)
        isLastClip -> finish(done)
        else -> readNext(done.copy(index = next))
    }
}

private fun holdAtEnd(done: ReaderState, resumeIndex: Int, endedIndex: Int): Transition<ReaderState> {
    val log = ReaderLog.HeldAtEnd(endedIndex, isReverse = done.gear == Gear.REVERSE)
    return hold(done.copy(index = resumeIndex), HoldReason.SENTENCE_END, log)
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
