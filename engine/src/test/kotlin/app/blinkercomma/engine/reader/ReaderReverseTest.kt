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
    fun `R 중에 재생을 눌러도 기다리고, P로 바꾼 뒤 누르면 다음 문장을 읽는다`() {
        val inReverse = run(listOf(gear(1.seconds, Gear.REVERSE), clipEnded(2.seconds), press(3.seconds)))
        assertIs<ReaderPhase.Holding>(inReverse.state.phase)
        assertEquals(true, "gear=R → keep hold" in inReverse.logs)
        val parked = run(
            listOf(gear(1.seconds, Gear.REVERSE), clipEnded(2.seconds), gear(4.seconds, Gear.PARK), press(5.seconds)),
        )
        assertIs<ReaderPhase.Reading>(parked.state.phase)
    }

    @Test
    fun `R에서 D로 나오면 새 구간이라 다시 쉼표를 낼 수 있다`() {
        val events = listOf(
            gear(1.seconds, Gear.REVERSE),
            clipEnded(2.seconds),
            gear(4.seconds, Gear.DRIVE),
            press(5.seconds),
        )
        assertEquals(false, run(events).state.isCommaUsed)
    }
}
