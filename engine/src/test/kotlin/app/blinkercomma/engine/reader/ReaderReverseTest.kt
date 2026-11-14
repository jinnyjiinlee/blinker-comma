package app.blinkercomma.engine.reader

import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.support.ReaderScene.clipEnded
import app.blinkercomma.engine.support.ReaderScene.gear
import app.blinkercomma.engine.support.ReaderScene.press
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.logs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

/** R11 — 후진(R) 진입도 주의 순간: 레버 없이 그 문장 끝에서 멈추고, 이번 R 구간엔 한 번만. */
class ReaderReverseTest {
    @Test
    fun `읽는 중 R로 바꾸면 그 문장 끝에서 멈춘다`() {
        val result = run(listOf(gear(1.seconds, Gear.REVERSE), clipEnded(2.seconds)))
        assertIs<ReaderPhase.Holding>(result.state.phase)
        assertEquals(true, "reverse → comma armed" in result.logs)
        assertEquals(true, "clipEnd s=2 gear=R → hold" in result.logs)
    }

    @Test
    fun `R 구간에서 재개하면 같은 R 구간엔 다시 멈추지 않는다`() {
        val events = listOf(
            gear(1.seconds, Gear.REVERSE),
            clipEnded(2.seconds),
            press(3.seconds),
            clipEnded(5.seconds, index = 2),
        )
        assertIs<ReaderPhase.Reading>(run(events).state.phase)
    }

    @Test
    fun `R에서 D로 나오면 새 구간이라 다시 쉼표를 낼 수 있다`() {
        val events = listOf(
            gear(1.seconds, Gear.REVERSE),
            clipEnded(2.seconds),
            press(3.seconds),
            gear(4.seconds, Gear.DRIVE),
        )
        assertEquals(false, run(events).state.isCommaUsed)
    }
}
