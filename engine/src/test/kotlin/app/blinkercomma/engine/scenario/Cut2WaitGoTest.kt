package app.blinkercomma.engine.scenario

import app.blinkercomma.engine.support.StageRun
import app.blinkercomma.engine.support.readStageScript
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/** 설계서 시연 컷 2 「신호 대기 → 출발」 — 서 있을 때 켠 깜빡이는 쉼표를 안 만들고, 켠 채 출발하면 그 문장 끝에서 쉰다. */
class Cut2WaitGoTest {
    private val script = readStageScript("cut2_wait_go.tsv")
    private val result = StageRun().run(script)

    @Test
    fun `컷 2는 신호 대기 중 켠 깜빡이에 쉼표를 예약하지 않는다`() {
        assertEquals(1.seconds, result.firstLogAt("lever=ON speed=0 → no comma"))
    }

    @Test
    fun `컷 2는 출발하면 예약하고 그 문장 끝에서 멈췄다가 다음 문장을 읽는다`() {
        val fromArmed = result.videoLogs.map { it.line }.dropWhile { it != "moving → comma armed" }.take(5)
        val expected = listOf(
            "moving → comma armed",
            "clipEnd s=3 lever=ON → hold",
            "turnSignal=OFF",
            "OFF held 0.80s → play s=4",
            "clipStart s=4",
        )
        assertEquals(expected, fromArmed)
    }

    @Test
    fun `컷 2는 쉼표를 한 번 낸다`() {
        assertEquals(1, result.state.tally.commas)
    }

    @Test
    fun `컷 2에서 속도 0 준비를 빠뜨리면 서 있어도 예약된다`() {
        val broken = StageRun().run(script.filterNot { it.signal == "speed" && it.value == "0" })
        assertTrue(broken.logs.none { "no comma" in it.line })
    }
}
