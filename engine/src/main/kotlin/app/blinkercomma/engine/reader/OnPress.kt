package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition

// 운전자 누름 — P 화면 버튼과 핸들 미디어 키가 같은 길로 들어온다

/** 쉼표 중 ▶는 디바운스 없이 즉시 재개(R9) — 「자동 재개 끔」이어도 같다(R8). */
internal fun onPlayPressed(
    state: ReaderState,
    event: ReaderEvent.PlayPressed,
    params: CommaCardParams,
): Transition<ReaderState> {
    val pressed = Transition(state, listOf(toWriteLog(ReaderLog.PlayedByPress(event.source, state.index))))
    return when (state.phase) {
        ReaderPhase.Stopped -> pressed.then { startReading(it, event.at, params) }
        ReaderPhase.FocusPaused -> pressed.then { startReading(it, event.at, params, isFromStart = false) }
        is ReaderPhase.Holding -> resume(state, ReaderLog.PlayedByPress(event.source, state.index), event.at, params)
        is ReaderPhase.Reading -> Transition(state)
    }
}

internal fun onPausePressed(state: ReaderState, event: ReaderEvent.PausePressed): Transition<ReaderState> =
    if (state.phase == ReaderPhase.Stopped) {
        Transition(state)
    } else {
        stop(state, ReaderLog.StoppedByPress(event.source, state.index))
    }

/** 건너뛰기. 쉼표 중 「이전」은 방금 끝난 문장을 다시 듣는 길이다. 멈춰 있으면 자리만 옮긴다. */
internal fun onSkipPressed(
    state: ReaderState,
    event: ReaderEvent.SkipPressed,
    params: CommaCardParams,
): Transition<ReaderState> {
    val index = (state.index + event.step).coerceIn(0, state.clipCount - 1)
    val moved = Transition(state.copy(index = index), listOf(toWriteLog(ReaderLog.Skipped(event.step, index))))
    return when (state.phase) {
        is ReaderPhase.Reading -> moved.then { startReading(it, event.at, params) }
        is ReaderPhase.Holding -> moved.then { startReading(it, event.at, params) }
            .let { it.copy(effects = it.effects + Effect.ShowHoldGlyph(false)) }
        ReaderPhase.Stopped, ReaderPhase.FocusPaused -> moved.then { Transition(it.copy(phase = ReaderPhase.Stopped)) }
    }
}
