package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.support.ReaderScene.armAt
import app.blinkercomma.engine.support.ReaderScene.clipEnded
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.leverOff
import app.blinkercomma.engine.support.ReaderScene.params
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.ReaderScene.tick
import app.blinkercomma.engine.support.logs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/** 설계서 쉼표 규칙 R4 — 예약 뒤 2.5초 안에 문장이 안 끝나면 문장 중간에서 멈추고, 재개는 그 문장 처음부터. */
class ReaderEndWaitTest {
    private val limitAt = armAt + params.endWaitLimit
    private val justBefore = limitAt - 10.milliseconds
    private val justAfter = limitAt + 10.milliseconds

    @Test
    fun `예약 뒤 상한 바로 전이면 계속 읽는다`() {
        assertEquals(ReaderPhase.Reading(armedAt = armAt), run(listOf(lever(armAt), tick(justBefore))).state.phase)
    }

    @Test
    fun `예약 뒤 상한에 닿으면 문장 중간에서 멈춘다`() {
        assertEquals(ReaderPhase.Holding(HoldReason.MID_SENTENCE), run(listOf(lever(armAt), tick(limitAt))).state.phase)
    }

    @Test
    fun `예약 뒤 상한을 넘기면 문장 중간에서 멈춘다`() {
        assertEquals(
            ReaderPhase.Holding(HoldReason.MID_SENTENCE),
            run(listOf(lever(armAt), tick(justAfter))).state.phase,
        )
    }

    @Test
    fun `문장 중간에서 멈추면 재생을 멈추고 쉼표 표시를 켠다`() {
        val result = run(listOf(lever(armAt), tick(limitAt)))
        assertEquals(
            listOf(Effect.PauseClip, Effect.ShowHoldGlyph(true)),
            result.effects.filter {
                it is Effect.PauseClip || it is Effect.ShowHoldGlyph
            },
        )
    }

    @Test
    fun `문장 중간에서 멈추면 기다린 시간과 문장 번호를 로그에 남긴다`() {
        assertEquals("endWait 2.50s → hold mid s=2", run(listOf(lever(armAt), tick(limitAt))).logs.last())
    }

    @Test
    fun `상한 바로 전에 문장이 끝나면 문장 끝에서 멈춘다`() {
        val result = run(listOf(lever(armAt), clipEnded(justBefore)))
        assertEquals(ReaderPhase.Holding(HoldReason.SENTENCE_END), result.state.phase)
    }

    @Test
    fun `문장 중간에서 멈췄다 재개하면 같은 문장 처음부터 읽는다`() {
        val offAt = limitAt + 1000.milliseconds
        val result = run(listOf(lever(armAt), tick(limitAt), leverOff(offAt), tick(offAt + params.offDebounce)))
        assertEquals(
            Effect.PlayClip(1, isFromStart = true),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `예약 중 다음 시간 알림은 상한 시각이다`() {
        assertEquals(limitAt, run(listOf(lever(armAt))).state.nextDeadline(params))
    }
}
