package app.blinkercomma.ui

import app.blinkercomma.data.SpeedPermission
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.reader.HoldReason
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** 설계서 「화면 목록」·원칙 1(주행 중 조작 0)·원칙 4(확인 안 됨) — 어느 화면을 무엇으로 그리나. */
class ScreenInputsTest {
    private val movingMinMps = CommaCardParams.DEFAULT.movingMinMps

    /** 기본 장면: 기어 P · 정차 · 권한 허용 · 8문장 팩 첫 문장 앞. */
    private val parked = ScreenInputs(
        reader = ReaderState(clipCount = 8, gear = Gear.PARK, speedMps = 0.0),
        isAutoResume = true,
        isHoldGlyphOn = false,
        holdSide = TurnSignal.OFF,
        isDemoInjected = false,
        speedPermission = SpeedPermission.GRANTED,
        pack = PackLabel(title = "낭독 1과", credit = "퍼블릭 도메인 낭독"),
    )

    private fun ScreenInputs.withReader(change: ReaderState.() -> ReaderState) = copy(reader = reader.change())

    private fun ScreenInputs.park() = assertIs<ScreenUiState.Park>(toUiState())

    @Test
    fun `기어 P면 P 화면이다`() {
        assertIs<ScreenUiState.Park>(parked.toUiState())
    }

    @Test
    fun `기어 D면 D 화면이다`() {
        assertIs<ScreenUiState.Drive>(parked.withReader { copy(gear = Gear.DRIVE) }.toUiState())
    }

    @Test
    fun `기어 R이면 D 화면이다`() {
        assertIs<ScreenUiState.Drive>(parked.withReader { copy(gear = Gear.REVERSE) }.toUiState())
    }

    @Test
    fun `기어 N이면 D 화면이다`() {
        assertIs<ScreenUiState.Drive>(parked.withReader { copy(gear = Gear.NEUTRAL) }.toUiState())
    }

    @Test
    fun `기어를 몰라도 이동 문턱 바로 위면 D 화면이다`() {
        val moving = parked.withReader { copy(gear = null, speedMps = movingMinMps + 0.01) }
        assertIs<ScreenUiState.Drive>(moving.toUiState())
    }

    @Test
    fun `기어를 모르고 속도가 이동 문턱과 같으면 P 화면이다`() {
        val atLimit = parked.withReader { copy(gear = null, speedMps = movingMinMps) }
        assertIs<ScreenUiState.Park>(atLimit.toUiState())
    }

    @Test
    fun `기어를 모르고 이동 문턱 바로 아래면 P 화면이다`() {
        val creeping = parked.withReader { copy(gear = null, speedMps = movingMinMps - 0.01) }
        assertIs<ScreenUiState.Park>(creeping.toUiState())
    }

    @Test
    fun `정차 중 속도 권한을 아직 안 물었으면 권한 안내다`() {
        val firstRun = parked.copy(speedPermission = SpeedPermission.NOT_ASKED)
        assertEquals(ScreenUiState.AskSpeedPermission, firstRun.toUiState())
    }

    @Test
    fun `주행 중이면 권한을 안 물었어도 D 화면이다`() {
        val firstRunDriving = parked.copy(speedPermission = SpeedPermission.NOT_ASKED)
            .withReader { copy(gear = Gear.DRIVE) }
        assertIs<ScreenUiState.Drive>(firstRunDriving.toUiState())
    }

    @Test
    fun `권한을 거부했으면 다시 묻지 않고 P 화면이다`() {
        assertIs<ScreenUiState.Park>(parked.copy(speedPermission = SpeedPermission.DENIED).toUiState())
    }

    @Test
    fun `속도를 못 받으면 속도 확인 안 됨 한 줄이다`() {
        val noSpeed = parked.withReader { copy(speedMps = null) }
        assertEquals(listOf(MissingSignal.SPEED), noSpeed.park().missingSignals)
    }

    @Test
    fun `기어를 못 받으면 기어 확인 안 됨 한 줄이다`() {
        val noGear = parked.withReader { copy(gear = null) }
        assertEquals(listOf(MissingSignal.GEAR), noGear.park().missingSignals)
    }

    @Test
    fun `앱을 막 띄워 첫 신호를 기다리는 동안엔 기어를 몰라도 확인 안 됨 줄이 없다`() {
        val justLaunched = parked.copy(isAwaitingFirstSignals = true).withReader { copy(gear = null) }
        assertEquals(emptyList(), justLaunched.park().missingSignals)
    }

    @Test
    fun `신호를 다 받으면 확인 안 됨 줄이 없다`() {
        assertEquals(emptyList(), parked.park().missingSignals)
    }

    @Test
    fun `쉼표 중이면 D 화면에 쉼표 표시를 넘긴다`() {
        val holding = parked.copy(isHoldGlyphOn = true, holdSide = TurnSignal.RIGHT)
            .withReader { copy(gear = Gear.DRIVE) }
        val expected = ScreenUiState.Drive(isHoldGlyphOn = true, holdSide = HoldSide.RIGHT, isDemoInjected = false)
        assertEquals(expected, holding.toUiState())
    }

    @Test
    fun `레버 LEFT로 쉼표에 들어가면 D 화면도 LEFT에서 들어온다`() {
        val holding = parked.copy(isHoldGlyphOn = true, holdSide = TurnSignal.LEFT)
            .withReader { copy(gear = Gear.DRIVE) }
        assertEquals(HoldSide.LEFT, (holding.toUiState() as ScreenUiState.Drive).holdSide)
    }

    @Test
    fun `레버 RIGHT로 쉼표에 들어가면 D 화면도 RIGHT에서 들어온다`() {
        val holding = parked.copy(isHoldGlyphOn = true, holdSide = TurnSignal.RIGHT)
            .withReader { copy(gear = Gear.DRIVE) }
        assertEquals(HoldSide.RIGHT, (holding.toUiState() as ScreenUiState.Drive).holdSide)
    }

    @Test
    fun `쉼표 단계면 P 화면 재생 판은 쉼표 중이다`() {
        val holding = parked.withReader { copy(phase = ReaderPhase.Holding(HoldReason.SENTENCE_END)) }
        assertEquals(Playback.HOLDING, holding.park().playback)
    }

    @Test
    fun `앱을 막 열면 재생 판은 멈춤이다`() {
        assertEquals(Playback.STOPPED, parked.park().playback)
    }

    @Test
    fun `문장 번호는 1부터 센다`() {
        val third = parked.withReader { copy(index = 2) }
        assertEquals(3, third.park().sentenceNumber)
    }

    @Test
    fun `팩이 비면 문장 번호는 0이다`() {
        val empty = parked.withReader { copy(clipCount = 0) }
        assertEquals(0, empty.park().sentenceNumber)
    }

    @Test
    fun `오늘 카운트는 엔진 카운트 그대로다`() {
        val counted = parked.withReader { copy(tally = tally.copy(sentences = 12, commas = 2)) }
        assertEquals(12 to 2, counted.park().let { it.todaySentences to it.todayCommas })
    }
}
