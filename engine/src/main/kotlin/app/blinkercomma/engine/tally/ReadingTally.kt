package app.blinkercomma.engine.tally

/** P 화면 「오늘 N문장 · 쉼표 M번」의 실제 카운트(설계서 원칙 4 — 고정 숫자 금지). */
data class ReadingTally(val sentences: Int = 0, val commas: Int = 0) {
    fun addSentence(): ReadingTally = copy(sentences = sentences + 1)

    fun addComma(): ReadingTally = copy(commas = commas + 1)
}
