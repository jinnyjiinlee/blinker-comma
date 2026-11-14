package app.blinkercomma.engine.reader

import app.blinkercomma.engine.comma.ArmDecision
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.comma.classifyArm
import app.blinkercomma.engine.log.ReaderLog
import app.blinkercomma.engine.log.toWriteLog
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition
import app.blinkercomma.engine.signal.CarEvent
import kotlin.time.Duration

internal fun onCarEvent(state: ReaderState, event: CarEvent, params: CommaCardParams): Transition<ReaderState> =
    when (event) {
        is CarEvent.TurnSignalChanged -> onLever(state, event, params)
        is CarEvent.GearChanged -> {
            val seen = if (event.gear == state.gear) emptyList() else listOf(toWriteLog(ReaderLog.GearSeen(event.gear)))
            val isNewGear = event.gear != state.gear
            val updated = state.copy(gear = event.gear, isCommaUsed = state.isCommaUsed && !isNewGear)
            Transition(updated, seen).then { trackArm(it, event.at, params) }
        }
        is CarEvent.SpeedChanged -> trackArm(state.copy(speedMps = event.speedMps), event.at, params)
    }

/** 레버가 내려오면 이번 구간의 쉼표 사용 표시도 풀린다(R6). 왼쪽↔오른쪽은 ON 구간이 이어진 것. */
private fun onLever(
    state: ReaderState,
    event: CarEvent.TurnSignalChanged,
    params: CommaCardParams,
): Transition<ReaderState> {
    val isFlipped = event.signal.isOn != state.lever.isOn
    val updated = state.copy(lever = event.signal, isCommaUsed = state.isCommaUsed && event.signal.isOn)
    val seen = if (event.signal == state.lever) {
        emptyList()
    } else {
        listOf(
            toWriteLog(ReaderLog.TurnSignalSeen(event.signal)),
        )
    }
    val next = if (isFlipped) flipLever(updated, event.at, params) else trackArm(updated, event.at, params)
    return Transition(next.state, seen + next.effects)
}

private fun flipLever(state: ReaderState, at: Duration, params: CommaCardParams): Transition<ReaderState> =
    when (val phase = state.phase) {
        is ReaderPhase.Reading -> Transition(state, noCommaLogs(state, params)).then { trackArm(it, at, params) }
        is ReaderPhase.Holding -> moveLeverWhileHolding(state, phase, at, params)
        ReaderPhase.Stopped, ReaderPhase.FocusPaused -> Transition(state)
    }

/** 레버를 올렸는데 예약하지 않는 까닭을 로그에 — 신호 대기(R3)·D 아님. */
private fun noCommaLogs(state: ReaderState, params: CommaCardParams): List<Effect> {
    val entry = when (classifyArm(state.snapshot, params)) {
        ArmDecision.SKIP_STOPPED -> ReaderLog.NoCommaStopped(state.speedMps ?: 0.0)
        ArmDecision.SKIP_NOT_DRIVE -> ReaderLog.NoCommaNotDrive(state.gear)
        ArmDecision.ARMED, ArmDecision.ARMED_REVERSE, ArmDecision.LEVER_OFF -> null
    }
    return listOfNotNull(entry?.let(::toWriteLog))
}

/** 쉼표 중 레버: 내려오면 디바운스 시작(R5), 다시 올라가면 리셋, 자동 재개를 껐으면 그대로(R8). */
private fun moveLeverWhileHolding(
    state: ReaderState,
    phase: ReaderPhase.Holding,
    at: Duration,
    params: CommaCardParams,
): Transition<ReaderState> = when {
    state.lever.isOn -> Transition(state.copy(phase = phase.copy(offSince = null)))
    params.isAutoResume -> resumeWhenHeld(state.copy(phase = phase.copy(offSince = at)), at, at, params)
    else -> Transition(state, listOf(toWriteLog(ReaderLog.KeptHold)))
}

/** 낭독 중에만 예약을 맞춘다. 쉼표 중엔 정차·기어 변경이 와도 그대로(R7). */
private fun trackArm(state: ReaderState, at: Duration, params: CommaCardParams): Transition<ReaderState> {
    val phase = state.phase
    if (phase !is ReaderPhase.Reading) return Transition(state)
    val isArmed = canArm(state, params)
    return when {
        isArmed && phase.armedAt == null ->
            Transition(state.copy(phase = ReaderPhase.Reading(at)), listOf(toWriteLog(armLog(state))))
        !isArmed && phase.armedAt != null ->
            Transition(state.copy(phase = ReaderPhase.Reading()), listOf(toWriteLog(ReaderLog.CommaCancelled)))
        else -> Transition(state)
    }
}
