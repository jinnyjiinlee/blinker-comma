package app.blinkercomma.engine.tally

import app.blinkercomma.engine.support.ReaderScene.clipEnded
import app.blinkercomma.engine.support.ReaderScene.lever
import app.blinkercomma.engine.support.ReaderScene.leverOff
import app.blinkercomma.engine.support.ReaderScene.press
import app.blinkercomma.engine.support.ReaderScene.reading
import app.blinkercomma.engine.support.ReaderScene.run
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 설계서 원칙 4 — P 화면 「오늘 N문장 · 쉼표 M번」은 실제 카운트. 쉼표 수 ≤ 레버를 켠 구간 수(R6). */
class ReadingTallyTest {
    @Test
    fun `문장을 마치면 문장 수가 하나 는다`() {
        assertEquals(ReadingTally(sentences = 1), ReadingTally().addSentence())
    }

    @Test
    fun `쉼표를 내면 쉼표 수가 하나 는다`() {
        assertEquals(ReadingTally(commas = 1), ReadingTally().addComma())
    }

    @Test
    fun `레버를 세 번 켜도 문장 끝을 넘긴 구간만 쉼표로 센다`() {
        val events = listOf(
            // 구간 1: 문장 끝 쉼표 → 핸들 키로 재개 → 켠 채 두 문장 더 (쉼표 1)
            lever(1.seconds), clipEnded(2.seconds, index = 1), press(3.seconds),
            clipEnded(4.seconds, index = 2), clipEnded(5.seconds, index = 3), leverOff(6.seconds),
            // 구간 2: 문장 끝 전에 내림 (쉼표 0)
            lever(7.seconds), leverOff(7.5.seconds),
            // 구간 3: 문장 끝 쉼표 (쉼표 1) — 구간 1 마지막 클립이 끝나 index 4를 읽는 중
            lever(8.seconds), clipEnded(9.seconds, index = 4),
        )
        assertEquals(2, run(events, initial = reading.copy(clipCount = 6)).state.tally.commas)
    }
}
