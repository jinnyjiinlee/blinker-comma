package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.AudioFocus.LOSS_TRANSIENT
import app.blinkercomma.engine.reader.PressSource.SCREEN
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.support.ReaderScene.armAt
import app.blinkercomma.engine.support.ReaderScene.focus
import app.blinkercomma.engine.support.ReaderScene.gear
import app.blinkercomma.engine.support.ReaderScene.holding
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.press
import app.blinkercomma.engine.support.ReaderScene.reading
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.ReaderScene.speed
import app.blinkercomma.engine.support.logs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 설계서 원칙 5(자동 재생 금지 — 첫 재생은 운전자가 ▶)와 P 화면·핸들 키 누름. */
class ReaderPressTest {
    private val stopped = reading.copy(phase = ReaderPhase.Stopped)
    private val pressAt = 3.seconds

    private fun pause(source: PressSource = SCREEN) = ReaderEvent.PausePressed(pressAt, source)

    private fun skip(step: Int) = ReaderEvent.SkipPressed(pressAt, step)

    @Test
    fun `앱을 켜고 차 신호가 들어와도 재생하지 않는다`() {
        val events = listOf(gear(1.seconds, Gear.DRIVE), speed(1.seconds, 10.0), lever(2.seconds))
        val result = run(events, initial = ReaderState(clipCount = 5))
        assertEquals(emptyList(), result.effects.filterIsInstance<Effect.PlayClip>())
    }

    @Test
    fun `정차 화면에서 재생을 누르면 지금 문장을 처음부터 읽는다`() {
        val result = run(listOf(press(pressAt, SCREEN)), initial = stopped)
        assertEquals(listOf("screen PLAY → play s=2", "clipStart s=2"), result.logs)
    }

    @Test
    fun `레버를 켠 채 달리는 중 재생을 누르면 그 순간 예약한다`() {
        val result = run(listOf(lever(armAt), press(pressAt, SCREEN)), initial = stopped)
        assertEquals(ReaderPhase.Reading(armedAt = pressAt), result.state.phase)
    }

    @Test
    fun `읽는 중 재생을 다시 눌러도 그대로다`() {
        assertEquals(emptyList(), run(listOf(press(pressAt))).effects)
    }

    @Test
    fun `읽는 중 정지를 누르면 멈추고 다음 재생은 그 문장 처음부터다`() {
        val result = run(listOf(pause(), press(5.seconds, SCREEN)))
        assertEquals(
            Effect.PlayClip(1, isFromStart = true),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `쉼표 중 정지를 누르면 쉼표 표시를 끄고 멈춘다`() {
        val result = run(listOf(pause(PressSource.MEDIA_KEY)), initial = holding)
        assertEquals(listOf(Effect.ShowHoldGlyph(false), Effect.WriteLog("mediaKey PAUSE → stop s=3")), result.effects)
    }

    @Test
    fun `멈춰 있을 때 정지를 누르면 아무것도 안 한다`() {
        assertEquals(emptyList(), run(listOf(pause()), initial = stopped).effects)
    }

    @Test
    fun `포커스로 멈춘 중 재생을 누르면 멈춘 자리부터 읽는다`() {
        val result = run(listOf(focus(1.seconds, LOSS_TRANSIENT), press(pressAt)))
        assertEquals(
            Effect.PlayClip(1, isFromStart = false),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `읽는 중 다음을 누르면 다음 문장을 처음부터 읽는다`() {
        val result = run(listOf(skip(+1)))
        assertEquals(listOf("skip +1 → s=3", "clipStart s=3"), result.logs)
    }

    @Test
    fun `마지막 문장에서 다음을 눌러도 마지막 문장에 머문다`() {
        assertEquals(4, run(listOf(skip(+1)), initial = reading.copy(index = 4)).state.index)
    }

    @Test
    fun `쉼표 중 이전을 누르면 방금 끝난 문장을 다시 읽고 쉼표 표시를 끈다`() {
        val result = run(listOf(skip(-1)), initial = holding)
        assertEquals(
            listOf(
                Effect.PlayClip(1, isFromStart = true),
                Effect.ShowHoldGlyph(false),
            ),
            result.effects.filterNot {
                it is Effect.WriteLog
            },
        )
    }

    @Test
    fun `문장 끝 쉼표 중 다음을 누르면 건너뛰지 않고 바로 다음 문장을 읽는다`() {
        val result = run(listOf(skip(+1)), initial = holding)
        assertEquals(holding.index, result.state.index)
        assertEquals(Effect.PlayClip(holding.index, isFromStart = true), result.effects.first { it is Effect.PlayClip })
    }

    @Test
    fun `포커스로 멈춘 중 건너뛰면 자리만 옮기고 멈춘다`() {
        val result = run(listOf(focus(1.seconds, LOSS_TRANSIENT), skip(+1)))
        assertEquals(reading.copy(phase = ReaderPhase.Stopped, index = 2), result.state)
    }
}
