package app.blinkercomma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.service.ReaderService
import app.blinkercomma.ui.ReaderScreen
import app.blinkercomma.ui.toUiState

/** 화면 하나짜리 앱. 서비스를 띄워 차 신호를 받기 시작하지만 재생은 ▶를 눌러야 시작한다(원칙 5). */
class MainActivity : ComponentActivity() {
    private val container by lazy { (application as BlinkerApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReaderRoute() }
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, ReaderService::class.java))
    }

    @Composable
    private fun ReaderRoute() {
        val state by container.session.state.collectAsStateWithLifecycle()
        val isAutoResume by container.settings.isAutoResume.collectAsStateWithLifecycle()
        val isHoldGlyphOn by container.session.isHoldGlyphOn.collectAsStateWithLifecycle()
        val injected by container.injection.kinds.collectAsStateWithLifecycle()
        ReaderScreen(
            state = toUiState(state, isAutoResume, isHoldGlyphOn, injected.isNotEmpty()),
            onPlayToggle = { onPlayToggle(state.phase) },
            onAutoResumeToggle = { container.settings.saveAutoResume(!isAutoResume) },
        )
    }

    private fun onPlayToggle(phase: ReaderPhase) {
        val at = monotonicNow()
        val event = if (phase == ReaderPhase.Stopped) {
            ReaderEvent.PlayPressed(at, PressSource.SCREEN)
        } else {
            ReaderEvent.PausePressed(at, PressSource.SCREEN)
        }
        container.session.onEvent(event)
    }
}
