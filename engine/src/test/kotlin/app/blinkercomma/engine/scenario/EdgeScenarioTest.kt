package app.blinkercomma.engine.scenario

import app.blinkercomma.engine.support.StageRun
import app.blinkercomma.engine.support.parseStage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 대본 밖 장면 — 문장 끝 전 취소(R2)·문장 중간 멈춤(R4)·내비 안내 포커스(R10). 형식은 `demo/script` 대본과 같다. */
class EdgeScenarioTest {
    private val ready =
        listOf("0\tvc\tgear\tDRIVE", "0\tvc\tspeed\t40", "0\tapp\tplay\tSCREEN", "-\tanchor\tlog\tclipStart")

    private fun stage(vararg lines: String) = StageRun().run(parseStage((ready + lines).joinToString("\n")))

    @Test
    fun `문장 끝 전에 깜빡이를 끄면 쉼표 없이 다음 문장으로 넘어간다`() {
        val result =
            stage("end-1.5\tvc\tturn_signal\tRIGHT", "end-0.5\tvc\tturn_signal\tOFF", "-\tanchor\tlog\tclipStart")
        assertEquals(
            Triple(null, 0, emptyList()),
            Triple(result.missingAnchor, result.state.tally.commas, result.glyphs),
        )
    }

    @Test
    fun `긴 문장 앞에서 깜빡이를 켜면 상한에 멈췄다가 그 문장을 처음부터 다시 읽는다`() {
        val result = stage(
            "-\tanchor\tlog\tclipStart",
            "0.5\tvc\tturn_signal\tRIGHT",
            "-\tanchor\tlog\thold mid",
            "1.0\tvc\tturn_signal\tOFF",
        )
        val fromHold = result.videoLogs.map { it.line }.dropWhile { "hold mid" !in it }.take(4)
        assertEquals(
            listOf("endWait 2.50s → hold mid s=3", "turnSignal=OFF", "OFF held 0.80s → play s=3", "clipStart s=3"),
            fromHold,
        )
    }

    @Test
    fun `내비 안내가 끼어들었다 끝나면 멈춘 자리부터 이어 읽는다`() {
        val result = stage("1.0\tapp\tfocus\tLOSS_TRANSIENT", "3.0\tapp\tfocus\tGAIN")
        assertEquals(4.6.seconds, result.firstLogAt("clipStart s=3"))
    }
}
