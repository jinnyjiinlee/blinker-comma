package app.blinkercomma.engine.support

import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** 대본 줄의 시각. 숫자 = 바로 위 anchor부터 · `end-X` = anchor 문장이 끝나기 X초 전 · `-` = anchor 줄. */
sealed interface StageTime {
    data class AfterAnchor(val offset: Duration) : StageTime

    data class BeforeEnd(val offset: Duration) : StageTime

    data object Anchor : StageTime
}

/** 무대 대본(`demo/script` 폴더의 TSV) 한 줄 — conductor·시나리오 테스트·종이 순서표가 같이 읽는다. */
data class StageLine(val time: StageTime, val hand: String, val signal: String, val value: String, val memo: String)

fun readStageScript(name: String): List<StageLine> =
    parseStage(requireNotNull(object {}.javaClass.getResource("/$name")) { "대본 없음: $name" }.readText())

fun parseStage(text: String): List<StageLine> = text.lines()
    .filter { it.isNotBlank() && !it.startsWith("#") }
    .map { line ->
        val columns = line.split("\t")
        StageLine(toStageTime(columns[0]), columns[1], columns[2], columns[3], columns.getOrElse(4) { "" })
    }

private fun toStageTime(column: String): StageTime = when {
    column == "-" -> StageTime.Anchor
    column.startsWith("end-") -> StageTime.BeforeEnd(toDuration(column.removePrefix("end-")))
    else -> StageTime.AfterAnchor(toDuration(column))
}

/** 초 글자 → Duration. ms로 반올림해 `3.7 + 0.8`이 정확히 4.5초가 되게. */
fun toDuration(seconds: String): Duration = (seconds.toDouble() * 1000).roundToLong().milliseconds
