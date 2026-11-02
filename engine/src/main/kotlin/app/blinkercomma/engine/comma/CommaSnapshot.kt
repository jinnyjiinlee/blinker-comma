package app.blinkercomma.engine.comma

import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal

/** 쉼표 예약 판정 입력 — 한 순간의 레버·기어·속도. `null` = 그 신호 확인 안 됨. */
data class CommaSnapshot(
    val lever: TurnSignal,
    val gear: Gear?,
    val speedMps: Double?,
)
