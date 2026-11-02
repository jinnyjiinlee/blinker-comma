package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.PressSource.MEDIA_KEY
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.support.ReaderScene.gear
import app.blinkercomma.engine.support.ReaderScene.holding
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.leverOff
import app.blinkercomma.engine.support.ReaderScene.params
import app.blinkercomma.engine.support.ReaderScene.press
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.ReaderScene.speed
import app.blinkercomma.engine.support.ReaderScene.tick
import app.blinkercomma.engine.support.logs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** 설계서 쉼표 규칙 R5(OFF 0.8초 유지 후 재개)·R7(멈춘 동안 정차·기어 변경은 무시)·R8(자동 재개 끔)·R9(핸들 키 즉시 재개). */
class ReaderResumeTest {
    private val offAt = 3.1.seconds
    private val resumeAt = offAt + params.offDebounce
    private val autoResumeOff = params.copy(isAutoResume = false)

    private fun phaseAfterOff(tickAt: Duration): ReaderPhase =
        run(listOf(leverOff(offAt), tick(tickAt)), initial = holding).state.phase

    @Test
    fun `레버가 디바운스 바로 전까지만 내려가 있으면 멈춤을 유지한다`() {
        assertEquals(
            ReaderPhase.Holding(HoldReason.SENTENCE_END, offSince = offAt),
            phaseAfterOff(resumeAt - 10.milliseconds),
        )
    }

    @Test
    fun `레버가 디바운스만큼 내려가 있으면 다음 문장을 읽는다`() {
        assertEquals(ReaderPhase.Reading(), phaseAfterOff(resumeAt))
    }

    @Test
    fun `레버가 디바운스를 넘겨 내려가 있으면 다음 문장을 읽는다`() {
        assertEquals(ReaderPhase.Reading(), phaseAfterOff(resumeAt + 10.milliseconds))
    }

    @Test
    fun `디바운스 뒤 재개는 다음 문장을 처음부터 읽고 유지 시간을 로그에 남긴다`() {
        val result = run(listOf(leverOff(offAt), tick(resumeAt)), initial = holding)
        assertEquals(listOf("turnSignal=OFF", "OFF held 0.80s → play s=3", "clipStart s=3"), result.logs)
    }

    @Test
    fun `쉼표 중 다음 시간 알림은 디바운스가 끝나는 시각이다`() {
        assertEquals(resumeAt, run(listOf(leverOff(offAt)), initial = holding).state.nextDeadline(params))
    }

    @Test
    fun `디바운스 중 레버가 다시 올라가면 처음부터 다시 잰다`() {
        val events = listOf(leverOff(offAt), lever(3.5.seconds), leverOff(3.7.seconds), tick(resumeAt))
        assertEquals(
            ReaderPhase.Holding(HoldReason.SENTENCE_END, offSince = 3.7.seconds),
            run(events, initial = holding).state.phase,
        )
    }

    @Test
    fun `디바운스가 0이면 레버가 내려오는 순간 다음 문장을 읽는다`() {
        val result = run(listOf(leverOff(offAt)), initial = holding, params = params.copy(offDebounce = Duration.ZERO))
        assertEquals(ReaderPhase.Reading(), result.state.phase)
    }

    @Test
    fun `쉼표 중 정차하고 P로 바꿔도 멈춤을 유지한다`() {
        val result = run(listOf(speed(2.5.seconds, 0.0), gear(3.seconds, Gear.PARK)), initial = holding)
        assertEquals(ReaderPhase.Holding(HoldReason.SENTENCE_END), result.state.phase)
    }

    @Test
    fun `자동 재개를 끄면 레버가 내려와도 멈춤을 유지한다`() {
        val result = run(listOf(leverOff(offAt), tick(10.seconds)), initial = holding, params = autoResumeOff)
        assertEquals(ReaderPhase.Holding(HoldReason.SENTENCE_END), result.state.phase)
    }

    @Test
    fun `자동 재개를 끄면 레버가 내려올 때 멈춤 유지를 로그에 남긴다`() {
        val result = run(listOf(leverOff(offAt)), initial = holding, params = autoResumeOff)
        assertEquals("autoResume=off → keep hold", result.logs.last())
    }

    @Test
    fun `자동 재개를 꺼도 핸들 키를 누르면 다음 문장을 읽는다`() {
        val result =
            run(listOf(leverOff(offAt), press(5.seconds, MEDIA_KEY)), initial = holding, params = autoResumeOff)
        assertEquals(listOf("mediaKey PLAY → play s=3", "clipStart s=3"), result.logs.takeLast(2))
    }

    @Test
    fun `쉼표 중 핸들 키를 누르면 레버가 올라가 있어도 바로 다음 문장을 읽는다`() {
        val result = run(listOf(press(2.5.seconds, MEDIA_KEY)), initial = holding)
        assertEquals(
            Effect.PlayClip(2, isFromStart = true),
            result.effects.filterIsInstance<Effect.PlayClip>().single(),
        )
    }

    @Test
    fun `쉼표 표시는 다시 읽기 명령 뒤에 끈다`() {
        val effects = run(listOf(press(2.5.seconds, MEDIA_KEY)), initial = holding).effects
        assertEquals(Effect.ShowHoldGlyph(false), effects.last())
    }
}
