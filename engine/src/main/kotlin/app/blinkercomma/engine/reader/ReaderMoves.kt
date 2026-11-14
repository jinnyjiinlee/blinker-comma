package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.comma.classifyArm
import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition
import app.blinkercomma.engine.signal.Gear
import kotlin.time.Duration

// 여러 이벤트가 같이 쓰는 단계 이동 — 재생 시작·쉼표·재개·정지

/** 예약 로그 — 후진(R11)과 깜빡이(R1)를 로그에서 구분한다. */
internal fun armLog(state: ReaderState): ReaderLog =
    if (state.gear == Gear.REVERSE) ReaderLog.ArmedReverse else ReaderLog.Armed

/** 쉼표를 예약할 수 있나 — 이번 레버 구간(후진은 이번 R 구간)에 이미 쉼표를 냈으면 안 된다(R6·R11). */
internal fun canArm(state: ReaderState, params: CommaCardParams): Boolean =
    !state.isCommaUsed && classifyArm(state.snapshot, params).isArmed

/** 지금 문장을 재생한다. 레버를 켠 채 달리는 중이면 이 순간부터 쉼표 예약(R1). */
internal fun startReading(
    state: ReaderState,
    at: Duration,
    params: CommaCardParams,
    isFromStart: Boolean = true,
): Transition<ReaderState> {
    val isArmed = canArm(state, params)
    val play = listOf(Effect.PlayClip(state.index, isFromStart), toWriteLog(ReaderLog.ClipStarted(state.index)))
    val arm = if (isArmed) listOf(toWriteLog(armLog(state))) else emptyList()
    return Transition(state.copy(phase = ReaderPhase.Reading(at.takeIf { isArmed })), play + arm)
}

/** 쉼표 시작. 이번 레버 구간의 쉼표를 쓰고(R6) 카운트를 올린다. 문장 끝이면 소리는 이미 멎어 있다. */
internal fun hold(state: ReaderState, reason: HoldReason, cause: ReaderLog): Transition<ReaderState> {
    val held = state.copy(phase = ReaderPhase.Holding(reason), isCommaUsed = true, tally = state.tally.addComma())
    val pause = if (reason == HoldReason.MID_SENTENCE) listOf(Effect.PauseClip) else emptyList()
    return Transition(held, pause + listOf(Effect.ShowHoldGlyph(true), toWriteLog(cause), Effect.SaveTally(held.tally)))
}

/** 쉼표를 풀고 지금 문장을 처음부터. ⏸는 소리가 다시 나는 순간(재생 명령 뒤)에 끈다. */
internal fun resume(
    state: ReaderState,
    cause: ReaderLog,
    at: Duration,
    params: CommaCardParams,
): Transition<ReaderState> {
    val started = startReading(state, at, params)
    return Transition(started.state, listOf(toWriteLog(cause)) + started.effects + Effect.ShowHoldGlyph(false))
}

/** 레버가 [CommaCardParams.offDebounce] 동안 내려가 있었으면 재개(R5). */
internal fun resumeWhenHeld(
    state: ReaderState,
    offSince: Duration,
    at: Duration,
    params: CommaCardParams,
): Transition<ReaderState> {
    val held = at - offSince
    return if (held >= params.offDebounce) {
        resume(state, ReaderLog.ResumedAfterOff(held, state.index), at, params)
    } else {
        Transition(state)
    }
}

/** 재생을 멈추고 쉼표 표시도 끈다. 다음 재생은 지금 문장 처음부터. */
internal fun stop(state: ReaderState, cause: ReaderLog): Transition<ReaderState> {
    val silence = when (state.phase) {
        is ReaderPhase.Reading -> listOf(Effect.PauseClip)
        is ReaderPhase.Holding -> listOf(Effect.ShowHoldGlyph(false))
        ReaderPhase.Stopped, ReaderPhase.FocusPaused -> emptyList()
    }
    return Transition(state.copy(phase = ReaderPhase.Stopped), silence + toWriteLog(cause))
}
