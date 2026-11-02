package app.blinkercomma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.blinkercomma.AppContainer
import app.blinkercomma.data.LoadedPack

/** 앱 상태 흐름 여섯 개를 모아 [toUiState] 한 번 — 화면은 이 결과만 본다. */
@Composable
fun collectScreenUiState(container: AppContainer): ScreenUiState {
    val reader by container.session.state.collectAsStateWithLifecycle()
    val isAutoResume by container.settings.isAutoResume.collectAsStateWithLifecycle()
    val isHoldGlyphOn by container.session.isHoldGlyphOn.collectAsStateWithLifecycle()
    val injected by container.injection.kinds.collectAsStateWithLifecycle()
    val speedPermission by container.speedPermission.status.collectAsStateWithLifecycle()
    val pack = (container.pack as? LoadedPack.Ready)?.pack
    val label = PackLabel(title = pack?.title.orEmpty(), credit = pack?.credit.orEmpty())
    return ScreenInputs(reader, isAutoResume, isHoldGlyphOn, injected.isNotEmpty(), speedPermission, label).toUiState()
}
