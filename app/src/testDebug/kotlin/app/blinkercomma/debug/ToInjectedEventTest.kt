package app.blinkercomma.debug

import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

/** 시연 주입 한 줄 → 엔진 이벤트(디버그 빌드 전용 — 설계서 원칙 10). */
class ToInjectedEventTest {
    private val at = 12.seconds

    @Test
    fun `깜빡이 주입은 레버 이벤트가 된다`() {
        val expected = ReaderEvent.Car(CarEvent.TurnSignalChanged(at, TurnSignal.RIGHT))
        assertEquals(expected, toInjectedEvent("turn_signal", "RIGHT", at))
    }

    @Test
    fun `기어 주입은 기어 이벤트가 된다`() {
        assertEquals(ReaderEvent.Car(CarEvent.GearChanged(at, Gear.DRIVE)), toInjectedEvent("gear", "DRIVE", at))
    }

    @Test
    fun `속도 주입은 m_s 그대로 속도 이벤트가 된다`() {
        assertEquals(ReaderEvent.Car(CarEvent.SpeedChanged(at, 8.33)), toInjectedEvent("speed_mps", "8.33", at))
    }

    @Test
    fun `포커스 주입은 포커스 이벤트가 된다`() {
        val expected = ReaderEvent.FocusChanged(at, AudioFocus.LOSS_TRANSIENT)
        assertEquals(expected, toInjectedEvent("focus", "LOSS_TRANSIENT", at))
    }

    @Test
    fun `핸들 키 PLAY 주입은 핸들 키 누름이 된다`() {
        assertEquals(ReaderEvent.PlayPressed(at, PressSource.MEDIA_KEY), toInjectedEvent("media_key", "PLAY", at))
    }

    @Test
    fun `핸들 키 NEXT 주입은 다음 문장 건너뛰기가 된다`() {
        assertEquals(ReaderEvent.SkipPressed(at, step = 1), toInjectedEvent("media_key", "NEXT", at))
    }

    @Test
    fun `모르는 신호 이름이면 무시한다`() {
        assertNull(toInjectedEvent("door", "OPEN", at))
    }

    @Test
    fun `모르는 값이면 무시한다`() {
        assertNull(toInjectedEvent("turn_signal", "HAZARD", at))
    }
}
