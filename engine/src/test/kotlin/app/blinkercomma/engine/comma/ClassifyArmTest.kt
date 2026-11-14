package app.blinkercomma.engine.comma

import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.test.Test
import kotlin.test.assertEquals

/** 설계서 쉼표 규칙 R1(레버 ON + D + 이동 중이면 예약)·R3(서 있으면 예약 안 함)·원칙 4(속도 확인 안 됨). */
class ClassifyArmTest {
    private val params = CommaCardParams.DEFAULT
    private val lanechange = CommaSnapshot(lever = TurnSignal.RIGHT, gear = Gear.DRIVE, speedMps = 40 / 3.6)

    @Test
    fun `D로 달리며 레버를 올리면 예약한다`() {
        assertEquals(ArmDecision.ARMED, classifyArm(lanechange, params))
    }

    @Test
    fun `왼쪽 레버도 예약한다`() {
        assertEquals(ArmDecision.ARMED, classifyArm(lanechange.copy(lever = TurnSignal.LEFT), params))
    }

    @Test
    fun `레버가 내려가 있으면 레버 내림이다`() {
        assertEquals(ArmDecision.LEVER_OFF, classifyArm(lanechange.copy(lever = TurnSignal.OFF), params))
    }

    @Test
    fun `속도가 이동 문턱 바로 위면 예약한다`() {
        assertEquals(ArmDecision.ARMED, classifyArm(lanechange.copy(speedMps = params.movingMinMps + 0.01), params))
    }

    @Test
    fun `속도가 이동 문턱과 같으면 서 있는 것으로 본다`() {
        assertEquals(ArmDecision.SKIP_STOPPED, classifyArm(lanechange.copy(speedMps = params.movingMinMps), params))
    }

    @Test
    fun `속도가 이동 문턱 바로 아래면 서 있는 것으로 본다`() {
        assertEquals(
            ArmDecision.SKIP_STOPPED,
            classifyArm(lanechange.copy(speedMps = params.movingMinMps - 0.01), params),
        )
    }

    @Test
    fun `신호 대기 속도 0이면 예약하지 않는다`() {
        assertEquals(ArmDecision.SKIP_STOPPED, classifyArm(lanechange.copy(speedMps = 0.0), params))
    }

    @Test
    fun `P면 예약하지 않는다`() {
        assertEquals(ArmDecision.SKIP_NOT_DRIVE, classifyArm(lanechange.copy(gear = Gear.PARK), params))
    }

    @Test
    fun `R이면 레버 없이도 후진 쉼표를 예약한다`() {
        assertEquals(ArmDecision.ARMED_REVERSE, classifyArm(lanechange.copy(gear = Gear.REVERSE), params))
    }

    @Test
    fun `N이면 예약하지 않는다`() {
        assertEquals(ArmDecision.SKIP_NOT_DRIVE, classifyArm(lanechange.copy(gear = Gear.NEUTRAL), params))
    }

    @Test
    fun `기어를 확인 못 하면 예약하지 않는다`() {
        assertEquals(ArmDecision.SKIP_NOT_DRIVE, classifyArm(lanechange.copy(gear = null), params))
    }

    @Test
    fun `속도를 확인 못 하면 레버와 D만으로 예약한다`() {
        assertEquals(ArmDecision.ARMED, classifyArm(lanechange.copy(speedMps = null), params))
    }

    @Test
    fun `R이면 레버를 내려도 예약한다`() {
        assertEquals(
            ArmDecision.ARMED_REVERSE,
            classifyArm(lanechange.copy(gear = Gear.REVERSE, lever = TurnSignal.OFF, speedMps = 0.0), params),
        )
    }
}
