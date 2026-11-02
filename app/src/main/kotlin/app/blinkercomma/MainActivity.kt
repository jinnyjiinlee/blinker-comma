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
            ),
            onAllowSpeed = { askSpeed.launch(SpeedPermissionStore.SPEED_PERMISSION) },
        )
    }
}
