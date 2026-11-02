package app.blinkercomma.player

import app.blinkercomma.engine.pack.SentenceClip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** 재생 위치 = 단조 시계로 잰 재생 시간(플레이어 위치는 에뮬레이터에서 실제보다 느렸음 — D2 실측). */
class PlayingClipTest {
    private val sentence = SentenceClip(file = "s01.m4a", text = "probe", durationMs = 2_600)
    private val fromStart = PlayingClip(index = 0, fromMs = 0, startedAt = 10.seconds)

    @Test
    fun `처음부터 1초 재생하면 위치는 1초다`() {
        assertEquals(1_000L, fromStart.positionMs(11.seconds, sentence))
    }

    @Test
    fun `이어 듣기면 멈춘 자리에 재생 시간을 더한다`() {
        val resumed = fromStart.copy(fromMs = 700)
        assertEquals(1_200L, resumed.positionMs(10.seconds + 500.milliseconds, sentence))
    }

    @Test
    fun `클립 길이를 넘기면 위치는 클립 길이에서 멈춘다`() {
        assertEquals(2_600L, fromStart.positionMs(20.seconds, sentence))
    }

    @Test
    fun `클립을 모르면 재생 시간 그대로다`() {
        assertEquals(10_000L, fromStart.positionMs(20.seconds, null))
    }
}
