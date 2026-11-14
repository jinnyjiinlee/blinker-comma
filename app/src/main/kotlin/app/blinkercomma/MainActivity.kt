package app.blinkercomma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import app.blinkercomma.data.SpeedPermissionStore
import app.blinkercomma.service.ReaderService
import app.blinkercomma.ui.AppScreen
import app.blinkercomma.ui.collectScreenUiState
import app.blinkercomma.ui.park.ParkActions
import app.blinkercomma.ui.toScreenPress

/** 화면 하나짜리 앱. 서비스를 띄워 차 신호를 받기 시작하지만 재생은 ▶를 눌러야 시작한다(원칙 5). */
class MainActivity : ComponentActivity() {
    private val container by lazy { (application as BlinkerApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoute() }
    }

    override fun onStart() {
        super.onStart()
        container.speedPermission.refresh()
        startService(Intent(this, ReaderService::class.java))
    }

    /** 팩을 바꾸면 세션·플레이어를 새 팩으로 다시 만든다 — 앱을 다시 띄우는 게 가장 단순하고 확실하다(정차 화면에서만). */
    private fun switchPack() {
        container.settings.saveNextPack()
        stopService(Intent(this, ReaderService::class.java))
        startActivity(Intent(this, MainActivity::class.java).addFlags(RESTART_FLAGS))
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    @Composable
    private fun AppRoute() {
        val askSpeed = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            container.speedPermission.saveAsked()
        }
        AppScreen(
            state = collectScreenUiState(container),
            actions = ParkActions(
                onPlayToggle = {
                    container.session.onEvent(
                        container.session.state.value.phase.toScreenPress(monotonicNow()),
                    )
                },
                onAutoResumeToggle = { container.settings.saveAutoResume(!container.settings.isAutoResume.value) },
                onPackChange = ::switchPack,
            ),
            onAllowSpeed = { askSpeed.launch(SpeedPermissionStore.SPEED_PERMISSION) },
        )
    }
}

private const val RESTART_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
