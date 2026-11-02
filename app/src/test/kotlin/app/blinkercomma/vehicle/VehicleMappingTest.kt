package app.blinkercomma.vehicle

import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.test.Test
import kotlin.test.assertEquals

/** Pleos 값 → 엔진 값. 깜빡이 값은 에뮬레이터가 레버 값을 그대로 준다(setup T_results 2026-10-25 T9). */
class VehicleMappingTest {
    @Test
    fun `오른쪽 깜빡이는 오른쪽 레버로 본다`() {
        assertEquals(TurnSignal.RIGHT, TurnSignalValue.RIGHT.toTurnSignal())
    }

    @Test
    fun `깜빡이 꺼짐은 레버 내림으로 본다`() {
        assertEquals(TurnSignal.OFF, TurnSignalValue.OFF.toTurnSignal())
    }

    @Test
    fun `왼쪽 깜빡이는 왼쪽 레버로 본다`() {
        assertEquals(TurnSignal.LEFT, TurnSignalValue.LEFT.toTurnSignal())
    }

    @Test
    fun `P 기어는 정차로 본다`() {
        assertEquals(Gear.PARK, VehicleGear.GEAR_PARK.toGear())
    }

    @Test
    fun `기어를 모르면 D가 아닌 것으로 본다`() {
        assertEquals(Gear.NEUTRAL, VehicleGear.GEAR_UNKNOWN.toGear())
    }

    @Test
    fun `D 기어는 주행으로 본다`() {
        assertEquals(Gear.DRIVE, VehicleGear.GEAR_DRIVE.toGear())
    }
}
