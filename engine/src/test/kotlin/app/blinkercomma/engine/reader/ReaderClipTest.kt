package app.blinkercomma.engine.reader

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.support.ReaderScene.armAt
import app.blinkercomma.engine.support.ReaderScene.clipEnded
import app.blinkercomma.engine.support.ReaderScene.endAt
import app.blinkercomma.engine.support.ReaderScene.holding
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.reading
import app.blinkercomma.engine.support.ReaderScene.run
import app.blinkercomma.engine.support.logs
import app.blinkercomma.engine.tally.ReadingTally
import kotlin.test.Test
import kotlin.test.assertEquals

/** 문장 1개 = 클립 1개(설계서 「콘텐츠 팩」) — 클립 끝이 곧 문장 끝이고, 끝날 때마다 오늘 문장 수를 센다. */
class ReaderClipTest {
    private val lastClip = reading.copy(index = 4)

    @Test
    fun `문장이 끝나면 다음 문장을 처음부터 읽는다`() {
        val result = run(listOf(clipEnded(endAt)))
        assertEquals(Effect.PlayClip(2, isFromStart = true), result.effects.first())
    }

    @Test
    fun `문장이 끝나면 오늘 문장 수를 세어 저장한다`() {
        assertEquals(Effect.SaveTally(ReadingTally(sentences = 1)), run(listOf(clipEnded(endAt))).effects.last())
    }

    @Test
    fun `쉼표로 멈추면 오늘 쉼표 수도 세어 저장한다`() {
        val result = run(listOf(lever(armAt), clipEnded(endAt)))
        assertEquals(Effect.SaveTally(ReadingTally(sentences = 1, commas = 1)), result.effects.last())
    }

    @Test
    fun `늦게 온 다른 문장의 끝 알림은 버린다`() {
        assertEquals(emptyList(), run(listOf(clipEnded(endAt, index = 0))).effects)
    }

    @Test
    fun `멈춘 중 온 끝 알림은 버린다`() {
        assertEquals(emptyList(), run(listOf(clipEnded(endAt, index = 2)), initial = holding).effects)
    }

    @Test
    fun `마지막 문장이 끝나면 예약돼 있어도 쉼표 없이 멈추고 처음으로 되감는다`() {
        val result = run(listOf(lever(armAt), clipEnded(endAt, index = 4)), initial = lastClip)
        assertEquals(ReaderPhase.Stopped to 0, result.state.phase to result.state.index)
    }

    @Test
    fun `마지막 문장이 끝나면 팩 끝을 로그에 남긴다`() {
        assertEquals(listOf("pack end → stop"), run(listOf(clipEnded(endAt, index = 4)), initial = lastClip).logs)
    }
}
