package app.blinkercomma.engine.scenario

import app.blinkercomma.engine.support.StageRun
import app.blinkercomma.engine.support.TimedLine
import app.blinkercomma.engine.support.readStageScript
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** 설계서 시연 컷 1 「숨 참기」 — 대본 `cut1_breath.tsv`를 엔진에 그대로 돌린다(합성 팩 문장 A = 2.6초). */
class Cut1BreathTest {
    private val script = readStageScript("cut1_breath.tsv")
    private val result = StageRun().run(script)

    @Test
    fun `컷 1은 로그 ⓐ부터 ⓓ까지 영상 시각에 맞춰 찍힌다`() {
        val expected = listOf(
            TimedLine(2.0.seconds, "turnSignal=RIGHT"),
            TimedLine(2.0.seconds, "moving → comma armed"),
            TimedLine(2.6.seconds, "clipEnd s=2 lever=ON → hold"),
            TimedLine(3.7.seconds, "turnSignal=OFF"),
            TimedLine(4.5.seconds, "OFF held 0.80s → play s=3"),
            TimedLine(4.5.seconds, "clipStart s=3"),
        )
        assertEquals(expected, result.videoLogs.drop(1).take(expected.size))
    }

    @Test
    fun `컷 1은 다음 문장이 첫 5초 안에 시작한다`() {
        assertTrue(result.firstLogAt("→ play") <= 5.seconds)
    }

    @Test
    fun `컷 1의 쉼표 표시는 문장 A 끝에 켜지고 다음 문장과 함께 꺼진다`() {
        val video = result.glyphs.map { (it.first - result.anchors.first()) to it.second }
        assertEquals(listOf(2600.milliseconds to true, 4500.milliseconds to false), video)
    }

    @Test
    fun `컷 1에서 D 기어 준비를 빠뜨리면 문장 끝 멈춤이 안 찍힌다`() {
        val broken = StageRun().run(script.filterNot { it.signal == "gear" && it.value == "DRIVE" })
        assertEquals("→ hold", broken.missingAnchor)
    }
}
