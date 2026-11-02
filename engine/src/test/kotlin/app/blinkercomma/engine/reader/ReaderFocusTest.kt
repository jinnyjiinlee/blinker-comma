package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.AudioFocus.GAIN
import app.blinkercomma.engine.reader.AudioFocus.LOSS
import app.blinkercomma.engine.reader.AudioFocus.LOSS_TRANSIENT
import app.blinkercomma.engine.support.ReaderScene.focus
import app.blinkercomma.engine.support.ReaderScene.holding
import app.blinkercomma.engine.support.ReaderScene.reading
import app.blinkercomma.engine.support.ReaderScene.run
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 설계서 쉼표 규칙 R10 — 남이 포커스를 잠시 가져가면 멈추고, 돌려주면 쉼표 중이 아닐 때만 이어 듣는다. 영구 LOSS면 정지. */
class ReaderFocusTest {
    private val lostAt = 1.seconds
    private val backAt = 4.seconds

    @Test
    fun `읽는 중 안내 음성이 포커스를 잠시 가져가면 멈춘다`() {
        val result = run(listOf(focus(lostAt, LOSS_TRANSIENT)))
        assertEquals(ReaderPhase.FocusPaused, result.state.phase)
    }

    @Test
    fun `포커스를 잠시 뺏기면 재생을 멈추는 명령을 낸다`() {
        assertEquals(
            listOf(Effect.PauseClip),
            run(listOf(focus(lostAt, LOSS_TRANSIENT))).effects.filterNot {
                it is Effect.WriteLog
            },
        )
    }

    @Test
    fun `포커스를 돌려받으면 멈춘 자리부터 이어 읽는다`() {
        val result = run(listOf(focus(lostAt, LOSS_TRANSIENT), focus(backAt, GAIN)))
        assertEquals(
            Effect.PlayClip(1, isFromStart = false),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `쉼표 중엔 포커스를 돌려받아도 재개하지 않는다`() {
        val result = run(listOf(focus(lostAt, LOSS_TRANSIENT), focus(backAt, GAIN)), initial = holding)
        assertEquals(ReaderPhase.Holding(HoldReason.SENTENCE_END), result.state.phase)
    }

    @Test
    fun `멈춰 있을 때 포커스를 받아도 재생하지 않는다`() {
        val result = run(listOf(focus(backAt, GAIN)), initial = reading.copy(phase = ReaderPhase.Stopped))
        assertEquals(emptyList(), result.effects.filterIsInstance<Effect.PlayClip>())
    }

    @Test
    fun `포커스를 영구히 잃으면 정지한다`() {
        assertEquals(ReaderPhase.Stopped, run(listOf(focus(lostAt, LOSS))).state.phase)
    }

    @Test
    fun `쉼표 중 포커스를 영구히 잃으면 쉼표 표시를 끈다`() {
        val result = run(listOf(focus(lostAt, LOSS)), initial = holding)
        assertEquals(listOf(Effect.ShowHoldGlyph(false), Effect.WriteLog("focus=LOSS")), result.effects)
    }

    @Test
    fun `포커스로 멈춘 중 영구히 잃으면 정지한다`() {
        val result = run(listOf(focus(lostAt, LOSS_TRANSIENT), focus(backAt, LOSS)))
        assertEquals(ReaderPhase.Stopped, result.state.phase)
    }
}
