package app.blinkercomma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.blinkercomma.AppContainer
import app.blinkercomma.data.LoadedPack
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

// 화면을 띄운 뒤 첫 콜백까지는 「아직 모름」 — 이 안엔 「확인 안 됨」 줄을 안 띄운다(D4 리허설: 앱을 다시 띄우면
// 기어 콜백 전 잠깐 「기어 신호 확인 안 됨」이 깜빡였다). 실측 지연은 docs/progress.md D4
private val FIRST_SIGNAL_WAIT = 3.seconds

/** 앱 상태 흐름 여섯 개 + 첫 신호 대기를 모아 [toUiState] 한 번 — 화면은 이 결과만 본다. */
@Composable
fun collectScreenUiState(container: AppContainer): ScreenUiState {
    val reader by container.session.state.collectAsStateWithLifecycle()
    val isAutoResume by container.settings.isAutoResume.collectAsStateWithLifecycle()
    val isHoldGlyphOn by container.session.isHoldGlyphOn.collectAsStateWithLifecycle()
    val injected by container.injection.kinds.collectAsStateWithLifecycle()
    val speedPermission by container.speedPermission.status.collectAsStateWithLifecycle()
    val isAwaitingFirstSignals by produceState(initialValue = true) {
        delay(FIRST_SIGNAL_WAIT)
        value = false
    }
    val pack = (container.pack as? LoadedPack.Ready)?.pack
    val label = PackLabel(title = pack?.title.orEmpty(), credit = pack?.credit.orEmpty())
    val inputs = ScreenInputs(reader, isAutoResume, isHoldGlyphOn, injected.isNotEmpty(), speedPermission, label)
    return inputs.copy(isAwaitingFirstSignals = isAwaitingFirstSignals).toUiState()
}
