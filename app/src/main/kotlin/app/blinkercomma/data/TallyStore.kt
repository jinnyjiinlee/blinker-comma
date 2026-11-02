package app.blinkercomma.data

import android.content.Context
import app.blinkercomma.engine.tally.ReadingTally
import java.time.LocalDate

/** 오늘 카운트(P 화면 「오늘 N문장 · 쉼표 M번」). 날짜 키는 여기서 붙인다 — 엔진은 날짜를 모른다. */
class TallyStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadToday(): ReadingTally {
        val day = LocalDate.now().toString()
        return ReadingTally(sentences = prefs.getInt("$day.$SENTENCES", 0), commas = prefs.getInt("$day.$COMMAS", 0))
    }

    fun save(tally: ReadingTally) {
        val day = LocalDate.now().toString()
        prefs.edit().putInt("$day.$SENTENCES", tally.sentences).putInt("$day.$COMMAS", tally.commas).apply()
    }

    private companion object {
        const val PREFS = "tally"
        const val SENTENCES = "sentences"
        const val COMMAS = "commas"
    }
}
