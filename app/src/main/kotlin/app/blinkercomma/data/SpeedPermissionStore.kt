package app.blinkercomma.data

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 속도 권한(CAR_SPEED, 런타임) 상태 — 허용됨 / 아직 안 물음 / 물었는데 거부. 「물었다」만 기기에 남긴다.
 * 시스템 설정에서 바꿀 수 있어 화면이 앞에 올 때마다 [refresh].
 */
class SpeedPermissionStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _status = MutableStateFlow(readStatus())
    val status: StateFlow<SpeedPermission> = _status.asStateFlow()

    fun refresh() {
        _status.value = readStatus()
    }

    /** 시스템 권한 창이 닫혔다(허용·거부 둘 다) — 안내 화면은 다시 띄우지 않는다. */
    fun saveAsked() {
        prefs.edit().putBoolean(ASKED, true).apply()
        refresh()
    }

    private fun readStatus(): SpeedPermission = when {
        context.checkSelfPermission(SPEED_PERMISSION) == PackageManager.PERMISSION_GRANTED -> SpeedPermission.GRANTED
        prefs.getBoolean(ASKED, false) -> SpeedPermission.DENIED
        else -> SpeedPermission.NOT_ASKED
    }

    companion object {
        /** `android.car.Car.PERMISSION_SPEED`와 같은 값 — 화면 쪽이 Car 라이브러리를 몰라도 되게 글자로. */
        const val SPEED_PERMISSION = "android.car.permission.CAR_SPEED"
        private const val PREFS = "permission"
        private const val ASKED = "speed_asked"
    }
}
