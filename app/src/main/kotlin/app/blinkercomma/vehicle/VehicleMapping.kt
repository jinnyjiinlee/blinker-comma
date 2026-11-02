package app.blinkercomma.vehicle

import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal

// Pleos 값 → 엔진 값. 엔진은 Pleos 타입을 모른다(모듈 경계). 리스너 등록은 D2의 PleosVehicleSource

fun TurnSignalValue.toTurnSignal(): TurnSignal = when (this) {
    TurnSignalValue.OFF -> TurnSignal.OFF
    TurnSignalValue.LEFT -> TurnSignal.LEFT
    TurnSignalValue.RIGHT -> TurnSignal.RIGHT
}

// 수동 변속 단(1~9)은 앞으로 가는 중이므로 D로 본다. 모름은 N — D가 아니면 쉼표를 예약하지 않는다
fun VehicleGear.toGear(): Gear = when (this) {
    VehicleGear.GEAR_PARK -> Gear.PARK
    VehicleGear.GEAR_REVERSE -> Gear.REVERSE
    VehicleGear.GEAR_NEUTRAL, VehicleGear.GEAR_UNKNOWN -> Gear.NEUTRAL
    else -> Gear.DRIVE
}
