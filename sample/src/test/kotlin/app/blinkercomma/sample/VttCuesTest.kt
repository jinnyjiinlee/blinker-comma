package app.blinkercomma.sample

import kotlin.test.Test
import kotlin.test.assertEquals

/** 자막 시각 줄만 큐가 되고, 번호·글자·머리 줄은 건너뛴다. 시각은 음원 안 밀리초. */
class VttCuesTest {
    @Test
    fun `시각 줄 두 개면 큐 두 개 — 시·분·초·밀리초를 밀리초로`() {
        val vtt = "WEBVTT\n\n00:00:00.600 --> 00:00:04.100\nA rat.\n\n1\n01:02:03.250 --> 01:02:04.000\nHe was.\n"
        assertEquals(listOf(Cue(600, 4100), Cue(3_723_250, 3_724_000)), readVttCues(vtt))
    }

    @Test
    fun `제출 샘플 자막은 47개 큐이고 시작이 끝보다 앞이다`() {
        val text = javaClass.classLoader!!.getResource("episode.vtt")?.readText()
            ?: java.io.File("src/main/assets/episode.vtt").readText()
        val cues = readVttCues(text)
        assertEquals(47, cues.size)
        assertEquals(true, cues.all { it.startMs < it.endMs })
    }
}
