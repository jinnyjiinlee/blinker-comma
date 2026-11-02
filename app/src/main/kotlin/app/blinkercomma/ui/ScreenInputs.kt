package app.blinkercomma.ui

import app.blinkercomma.data.SpeedPermission
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal

/**
 * [toUiState] 입력 묶음 — 엔진 상태 + 앱 쪽 상태(설정·⏸·시연 주입·권한·팩 글자).
 * [isAwaitingFirstSignals]: 화면을 막 띄워 첫 콜백을 기다리는 중 — 이때 null은 「못 받음」이 아니라 「아직 모름」이다.
 * [holdSide]: ⏸가 켜질 때의 레버 방향 — D 화면 진입 쪽(ui_plan.md P0-1).
 */
data class ScreenInputs(
    val reader: ReaderState,
    val isAutoResume: Boolean,
    val isHoldGlyphOn: Boolean,
    val holdSide: TurnSignal,
    val isDemoInjected: Boolean,
    val speedPermission: SpeedPermission,
    val pack: PackLabel,
    val isAwaitingFirstSignals: Boolean = false,
)

/** 팩에서 화면이 보여 주는 글자 둘 — 깨진 팩이면 빈 글자(줄이 숨는다). */
data class PackLabel(val title: String, val credit: String)

/**
 * 어느 화면인가. 주행이면 무조건 D 화면 — 권한 안내도 정차까지 미룬다(원칙 1). 기어를 못 받아도 이동 중이면 주행으로 본다.
 * 기어도 속도도 모르면 P 화면 + 「확인 안 됨」 줄(쉼표 예약은 어차피 D가 확인돼야 걸린다).
 */
fun ScreenInputs.toUiState(movingMinMps: Double = CommaCardParams.DEFAULT.movingMinMps): ScreenUiState = when {
    reader.isDriving(movingMinMps) -> ScreenUiState.Drive(isHoldGlyphOn, holdSide.toHoldSide(), isDemoInjected)
    speedPermission == SpeedPermission.NOT_ASKED -> ScreenUiState.AskSpeedPermission
    else -> toPark()
}

/** OFF는 ⏸가 꺼진 동안의 기본값일 뿐(그 사이엔 화면에 안 쓰인다) — 아무 쪽이나 되게 RIGHT로. */
private fun TurnSignal.toHoldSide(): HoldSide = when (this) {
    TurnSignal.LEFT -> HoldSide.LEFT
    TurnSignal.RIGHT, TurnSignal.OFF -> HoldSide.RIGHT
}

private fun ReaderState.isDriving(movingMinMps: Double): Boolean =
    gear in DRIVING_GEARS || (speedMps ?: 0.0) > movingMinMps

private val DRIVING_GEARS = setOf(Gear.DRIVE, Gear.REVERSE, Gear.NEUTRAL)

private fun ScreenInputs.toPark() = ScreenUiState.Park(
    playback = reader.phase.toPlayback(),
    sentenceNumber = if (reader.clipCount == 0) 0 else reader.index + 1,
    sentenceCount = reader.clipCount,
    todaySentences = reader.tally.sentences,
    todayCommas = reader.tally.commas,
    isAutoResume = isAutoResume,
    missingSignals = if (isAwaitingFirstSignals) emptyList() else reader.missingSignals(),
    packTitle = pack.title,
    credit = pack.credit,
    isDemoInjected = isDemoInjected,
)

private fun ReaderState.missingSignals() = listOfNotNull(
    MissingSignal.GEAR.takeIf { gear == null },
    MissingSignal.SPEED.takeIf { speedMps == null },
)

private fun ReaderPhase.toPlayback(): Playback = when (this) {
    ReaderPhase.Stopped -> Playback.STOPPED
    is ReaderPhase.Reading -> Playback.READING
    is ReaderPhase.Holding -> Playback.HOLDING
    ReaderPhase.FocusPaused -> Playback.FOCUS_PAUSED
}
