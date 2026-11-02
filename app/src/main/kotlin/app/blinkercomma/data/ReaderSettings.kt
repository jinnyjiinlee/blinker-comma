package app.blinkercomma.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** P 화면 설정 — 「자동 재개 켬·끔」(R8) 하나. 기본 끔(D6, M8 must_fix — 제출 서류·영상 문구와 맞춤). */
class ReaderSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _isAutoResume = MutableStateFlow(prefs.getBoolean(AUTO_RESUME, DEFAULT_AUTO_RESUME))
    val isAutoResume: StateFlow<Boolean> = _isAutoResume.asStateFlow()

    fun saveAutoResume(isOn: Boolean) {
        _isAutoResume.value = isOn
        prefs.edit().putBoolean(AUTO_RESUME, isOn).apply()
    }

    companion object {
        private const val PREFS = "settings"
        private const val AUTO_RESUME = "auto_resume"

        /** qa_M3 결함 1 — 제출 서류·영상이 「기본 끔」을 전제하므로 상수로 고정해 테스트로 지킨다. */
        const val DEFAULT_AUTO_RESUME = false
    }
}
