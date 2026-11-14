package app.blinkercomma.data

import android.content.Context

/** 이어 듣기 — 마지막으로 재생을 시작한 문장. 시동을 껐다 켜도 그 문장부터(팩이 바뀌면 처음부터). */
class PositionStore(context: Context, private val packKey: String) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(clipCount: Int): Int = when {
        clipCount <= 0 || prefs.getString(PACK, null) != packKey -> 0
        else -> prefs.getInt(INDEX, 0).coerceIn(0, clipCount - 1)
    }

    fun save(index: Int) {
        prefs.edit().putString(PACK, packKey).putInt(INDEX, index).apply()
    }

    private companion object {
        const val PREFS = "position"
        const val PACK = "pack"
        const val INDEX = "index"
    }
}
