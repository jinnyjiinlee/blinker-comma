package app.blinkercomma.engine.support

import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.tally.ReadingTally
import kotlin.time.Duration

data class TimedLine(val at: Duration, val line: String)

/** 대본을 끝까지(또는 anchor를 못 찾은 곳까지) 돌린 결과. 시각은 무대 시작부터. */
data class StageResult(
    val logs: List<TimedLine>,
    /** ⏸ 켜짐·꺼짐 순간. */
    val glyphs: List<Pair<Duration, Boolean>>,
    val tallies: List<Pair<Duration, ReadingTally>>,
    /** 찾은 anchor 시각(대본 순서). */
    val anchors: List<Duration>,
    /** 못 찾은 anchor 글자 — null이면 대본을 끝까지 돌았다. */
    val missingAnchor: String?,
    val state: ReaderState,
) {
    /** 첫 anchor부터 잰 시각과 로그 줄 — 영상 시각(첫 anchor = 영상 0초)과 같은 눈금. */
    val videoLogs: List<TimedLine> get() = logs.filter {
        it.at >= anchors.first()
    }.map { it.copy(at = it.at - anchors.first()) }

    fun firstLogAt(text: String): Duration = requireNotNull(
        videoLogs.firstOrNull { text in it.line },
    ) { "로그 없음: $text" }.at

    fun tallyAt(at: Duration): ReadingTally = tallies.lastOrNull { it.first <= at }?.second ?: ReadingTally()
}
