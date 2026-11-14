package app.blinkercomma.engine.scenario

import app.blinkercomma.engine.support.StageRun
import app.blinkercomma.engine.support.readStageScript
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** 시연 컷 4 「후진」(R11) — 레버 없이 R로 바꾸면 지금 문장 끝에서 멈추고, 후진 중 핸들 키는 무시, P 뒤 핸들 키로 다음 문장. */
class Cut4ReverseTest {
    private val script = readStageScript("cut4_reverse.tsv")
    private val result = StageRun().run(script)
    private val holdAt = result.anchors[1]

    @Test
    fun `컷 4는 후진 기어만으로 예약하고 핸들 키로 다음 문장을 읽는다`() {
        val lines = result.videoLogs.map { it.line }
        val fromReverse = lines.dropWhile { it != "reverse → comma armed" }.take(1)
        assertEquals(listOf("reverse → comma armed"), fromReverse)
        assertEquals(true, "gear=R → keep hold" in lines)
        assertEquals(true, "mediaKey PLAY → play s=3" in lines)
    }

    @Test
    fun `컷 4의 쉼표 표시는 주차 뒤 핸들 키를 누를 때까지 켜져 있다`() {
        val fromHold = result.glyphs.map { (it.first - holdAt) to it.second }
        assertEquals(listOf(Duration.ZERO to true, 5.seconds to false), fromHold)
    }
}
