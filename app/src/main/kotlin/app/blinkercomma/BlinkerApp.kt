package app.blinkercomma

import android.app.Application

/** 앱 프로세스 하나에 [AppContainer] 하나 — 화면·서비스·시연 주입이 같은 세션을 본다. */
class BlinkerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
