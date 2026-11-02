package app.blinkercomma.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.blinkercomma.BlinkerApp
import app.blinkercomma.monotonicNow

/**
 * 시연 주입(디버그 빌드 전용 — 설계서 원칙 10). VC 웹 API로 못 넣는 신호를 adb 브로드캐스트로 엔진에 넣는다:
 * `adb shell am broadcast -a app.blinkercomma.debug.INJECT --es signal focus --es value LOSS_TRANSIENT`.
 * 넣은 신호 이름은 화면 「시연 주입」 표시와 로그 `CommaInject`에 남는다 — 실제 신호 경로와 섞이지 않게.
 */
class DemoInjectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val signal = intent.getStringExtra(EXTRA_SIGNAL).orEmpty()
        val value = intent.getStringExtra(EXTRA_VALUE).orEmpty()
        val event = toInjectedEvent(signal, value, monotonicNow())
        if (event == null) {
            Log.w(TAG, "unknown inject $signal=$value")
            return
        }
        val container = (context.applicationContext as BlinkerApp).container
        Log.i(TAG, "demo inject $signal=$value")
        container.injection.mark(signal)
        container.session.onEvent(event)
    }

    private companion object {
        const val TAG = "CommaInject"
        const val EXTRA_SIGNAL = "signal"
        const val EXTRA_VALUE = "value"
    }
}
