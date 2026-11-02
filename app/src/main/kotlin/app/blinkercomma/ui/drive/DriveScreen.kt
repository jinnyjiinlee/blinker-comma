package app.blinkercomma.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.blinkercomma.ui.ScreenUiState
import app.blinkercomma.ui.component.DemoInjectedTag
import app.blinkercomma.ui.component.HoldGlyph
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace

/**
 * D 화면(주행) — 어두운 빈 화면. 쉼표 중에만 ⏸ 하나, 측후방 카메라 폭(780dp) 오른쪽 가운데(설계서 원칙 1·11).
 * 글자 0 · 누를 것 0. 시연 주입 표시는 디버그 빌드에서 주입한 뒤에만(원칙 10).
 */
@Composable
fun DriveScreen(state: ScreenUiState.Drive, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(CommaColors.background)) {
        Box(
            Modifier.fillMaxSize().padding(start = CommaSize.cameraOverlayWidth),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isHoldGlyphOn) HoldGlyph()
        }
        if (state.isDemoInjected) {
            DemoInjectedTag(Modifier.align(Alignment.BottomEnd).padding(CommaSpace.xlarge))
        }
    }
}
