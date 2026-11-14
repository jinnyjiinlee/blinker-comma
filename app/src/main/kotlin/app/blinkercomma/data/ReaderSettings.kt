package app.blinkercomma.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** P 화면 설정 — 「자동 재개 켬·끔」(R8, 기본 끔)과 낭독 팩 선택(기본 한국어 단편). */
class ReaderSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _isAutoResume = MutableStateFlow(prefs.getBoolean(AUTO_RESUME, DEFAULT_AUTO_RESUME))
    val isAutoResume: StateFlow<Boolean> = _isAutoResume.asStateFlow()

    /** 낭독 팩 폴더 — 앱 시작 때 한 번 읽는다(바꾸면 앱을 다시 시작해 새 팩으로). */
    val packFolder: String = prefs.getString(PACK, DEFAULT_PACK)?.takeIf { it in PACKS } ?: DEFAULT_PACK

    /** 다음 팩으로 바꿔 즉시 저장 — 저장 직후 프로세스를 다시 띄우므로 commit. */
    fun saveNextPack() {
        val next = PACKS[(PACKS.indexOf(packFolder) + 1) % PACKS.size]
        prefs.edit().putString(PACK, next).commit()
    }

    fun saveAutoResume(isOn: Boolean) {
        _isAutoResume.value = isOn
        prefs.edit().putBoolean(AUTO_RESUME, isOn).apply()
    }

    companion object {
        private const val PREFS = "settings"
        private const val AUTO_RESUME = "auto_resume"
        private const val PACK = "pack"

        /** 한국어 단편(기본) ↔ 영어 이솝 우화. */
        val PACKS = listOf("packs/hyun", "packs/aesop")
        val DEFAULT_PACK = PACKS.first()

        /** qa_M3 결함 1 — 제출 서류·영상이 「기본 끔」을 전제하므로 상수로 고정해 테스트로 지킨다. */
        const val DEFAULT_AUTO_RESUME = false
    }
}
