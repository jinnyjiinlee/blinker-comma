package app.blinkercomma.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** P 화면 설정 — 「자동 재개 켬·끔」(R8) 하나. 기본 켬. */
class ReaderSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _isAutoResume = MutableStateFlow(prefs.getBoolean(AUTO_RESUME, true))
    val isAutoResume: StateFlow<Boolean> = _isAutoResume.asStateFlow()

    fun saveAutoResume(isOn: Boolean) {
        _isAutoResume.value = isOn
        prefs.edit().putBoolean(AUTO_RESUME, isOn).apply()
    }

    private companion object {
        const val PREFS = "settings"
        const val AUTO_RESUME = "auto_resume"
    }
}
