package app.blinkercomma.player

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * D4 리허설 2회차 컷 2(docs/logs/d4_rehearsal_02_logcat.txt) — 끝난 문장 플레이어를 놓는(release 5.6초 막힘) 동안
 * 다음 문장 준비(prepare)가 같이 막혀 문장 3이 2.76초 늦게 났다. 놓기는 다음 준비가 끝난 뒤로 미룬다.
 */
class PlayerHandoffTest {
    /** 기본 장면: 문장 1이 재생 중. 플레이어는 이름(문장)으로 대신한다. */
    private fun readingFirst() = PlayerHandoff<String>().apply { start("s1") }

    @Test
    fun `문장이 시계로 끝나면 다음 문장이 시작돼도 아직 놓지 않는다`() {
        val handoff = readingFirst()
        handoff.finish()
        assertEquals(emptyList(), handoff.start("s2"))
    }

    @Test
    fun `다음 문장 준비가 끝나면 끝난 문장 플레이어를 놓는다`() {
        val handoff = readingFirst()
        handoff.finish()
        handoff.start("s2")
        assertEquals(listOf("s1"), handoff.afterPreload())
    }

    @Test
    fun `멈추면 재생 중인 것과 놓기를 기다리던 것을 바로 다 놓는다`() {
        val handoff = readingFirst()
        handoff.finish()
        handoff.start("s2")
        assertEquals(listOf("s2", "s1"), handoff.stop())
    }

    @Test
    fun `재생 중에 다른 문장을 열면 재생 중이던 것은 바로 놓는다`() {
        assertEquals(listOf("s1"), readingFirst().start("s5"))
    }

    @Test
    fun `끝난 문장이 둘 쌓이면 먼저 끝난 것은 바로 놓는다`() {
        val handoff = readingFirst()
        handoff.finish()
        handoff.start("s2")
        assertEquals(listOf("s1"), handoff.finish())
    }
}
