package app.blinkercomma.engine.scenario

import app.blinkercomma.engine.support.StageRun
import app.blinkercomma.engine.support.readStageScript
import app.blinkercomma.engine.tally.ReadingTally
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** 설계서 시연 컷 3 「자동 재개 끔 + 핸들 키」 — 레버를 내려도 기다리고, 핸들 키(시연 주입) 한 번에 다음 문장. */
class Cut3KeyResumeTest {
    private val script = readStageScript("cut3_key_resume.tsv")
    private val result = StageRun().run(script)
    private val holdAt = result.anchors[1]

    @Test
    fun `컷 3은 레버를 내려도 멈춤을 유지하고 핸들 키로 다음 문장을 읽는다`() {
        val fromOff = result.videoLogs.map { it.line }.dropWhile { it != "turnSignal=OFF" }.take(4)
        val expected =
            listOf("turnSignal=OFF", "autoResume=off → keep hold", "mediaKey PLAY → play s=3", "clipStart s=3")
        assertEquals(expected, fromOff)
    }

    @Test
    fun `컷 3의 쉼표 표시는 핸들 키를 누를 때까지 켜져 있다`() {
        val fromHold = result.glyphs.map { (it.first - holdAt) to it.second }
        assertEquals(listOf(Duration.ZERO to true, 3.seconds to false), fromHold)
    }

    @Test
    fun `컷 3 끝 P 화면 카운트는 실제로 마친 문장과 쉼표 수다`() {
        assertEquals(ReadingTally(sentences = 2, commas = 1), result.tallyAt(holdAt + 5.5.seconds))
    }

    @Test
    fun `컷 3에서 자동 재개 끔 준비를 빠뜨리면 레버로 재개되고 핸들 키가 정지가 된다`() {
        val broken = StageRun().run(script.filterNot { it.signal == "auto_resume" })
        val lines = broken.logs.map { it.line }
        assertTrue("OFF held 0.80s → play s=3" in lines && "mediaKey PAUSE → stop s=3" in lines)
    }
}
