package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import app.blinkercomma.engine.support.ReaderScene.armAt
import app.blinkercomma.engine.support.ReaderScene.clipEnded
import app.blinkercomma.engine.support.ReaderScene.endAt
import app.blinkercomma.engine.support.ReaderScene.gear
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.leverOff
import app.blinkercomma.engine.support.ReaderScene.press
import app.blinkercomma.engine.support.ReaderScene.reading
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.ReaderScene.speed
import app.blinkercomma.engine.support.ReaderScene.tick
import app.blinkercomma.engine.support.logs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 설계서 쉼표 규칙 R1(예약·문장 끝 멈춤)·R2(문장 끝 전 OFF면 취소)·R3(서 있으면 예약 안 함)·R6(레버 한 번에 쉼표 1번). */
class ReaderArmTest {
    @Test
    fun `달리며 레버를 올리면 그 순간 쉼표를 예약한다`() {
        assertEquals(ReaderPhase.Reading(armedAt = armAt), run(listOf(lever(armAt))).state.phase)
    }

    @Test
    fun `예약된 채 문장이 끝나면 다음 문장을 시작하지 않고 멈춘다`() {
        val result = run(listOf(lever(armAt), clipEnded(endAt)))
        assertEquals(ReaderPhase.Holding(HoldReason.SENTENCE_END), result.state.phase)
    }

    @Test
    fun `문장 끝에서 멈추면 재생 명령을 내지 않는다`() {
        val result = run(listOf(lever(armAt), clipEnded(endAt)))
        assertEquals(emptyList(), result.effects.filterIsInstance<Effect.PlayClip>())
    }

    @Test
    fun `문장 끝 전에 레버가 내려오면 쉼표 없이 다음 문장을 읽는다`() {
        val result = run(listOf(lever(armAt), leverOff(1.5.seconds), clipEnded(endAt)))
        assertEquals(
            Effect.PlayClip(2, isFromStart = true),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `문장 끝 전에 레버가 내려오면 예약 취소를 로그에 남긴다`() {
        assertEquals("comma cancelled", run(listOf(lever(armAt), leverOff(1.5.seconds))).logs.last())
    }

    @Test
    fun `서 있을 때 레버를 올리면 예약하지 않고 까닭을 로그에 남긴다`() {
        val result = run(listOf(lever(armAt)), initial = reading.copy(speedMps = 0.0))
        assertEquals(listOf("turnSignal=RIGHT", "lever=ON speed=0 → no comma"), result.logs)
    }

    @Test
    fun `P에서 레버를 올리면 예약하지 않고 까닭을 로그에 남긴다`() {
        val result = run(listOf(lever(armAt)), initial = reading.copy(gear = Gear.PARK))
        assertEquals(listOf("turnSignal=RIGHT", "lever=ON gear=PARK → no comma"), result.logs)
    }

    @Test
    fun `레버를 켠 채 출발하면 이동이 시작된 순간 예약한다`() {
        val result = run(listOf(lever(armAt), speed(3.seconds, 30 / 3.6)), initial = reading.copy(speedMps = 0.0))
        assertEquals(ReaderPhase.Reading(armedAt = 3.seconds), result.state.phase)
    }

    @Test
    fun `레버를 켠 채 D로 바꾸면 그 순간 예약한다`() {
        val result = run(listOf(lever(armAt), gear(3.seconds, Gear.DRIVE)), initial = reading.copy(gear = Gear.NEUTRAL))
        assertEquals(ReaderPhase.Reading(armedAt = 3.seconds), result.state.phase)
    }

    @Test
    fun `예약 중 서면 예약을 푼다`() {
        val result = run(listOf(lever(armAt), speed(1.5.seconds, 0.0)))
        assertEquals(ReaderPhase.Reading(armedAt = null), result.state.phase)
    }

    @Test
    fun `왼쪽에서 오른쪽으로 바로 바꿔도 처음 예약 순간을 유지한다`() {
        val result = run(listOf(lever(armAt, TurnSignal.LEFT), lever(1.5.seconds, TurnSignal.RIGHT)))
        assertEquals(ReaderPhase.Reading(armedAt = armAt), result.state.phase)
    }

    @Test
    fun `레버 한 번에 쉼표는 한 번만 낸다`() {
        val events =
            listOf(lever(armAt), clipEnded(endAt), press(3.seconds), clipEnded(5.seconds, index = 2), tick(9.seconds))
        assertEquals(1, run(events).state.tally.commas)
    }

    @Test
    fun `레버를 내렸다 다시 올리면 다시 예약한다`() {
        val events = listOf(lever(armAt), clipEnded(endAt), leverOff(3.seconds), tick(3.8.seconds), lever(5.seconds))
        assertEquals(ReaderPhase.Reading(armedAt = 5.seconds), run(events).state.phase)
    }
}
