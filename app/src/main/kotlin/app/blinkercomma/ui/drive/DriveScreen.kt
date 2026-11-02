package app.blinkercomma.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.blinkercomma.ui.HoldSide
import app.blinkercomma.ui.ScreenUiState
import app.blinkercomma.ui.component.CommaWatermark
import app.blinkercomma.ui.component.DemoInjectedTag
import app.blinkercomma.ui.component.HoldGlyph
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.HoldGlyphTransition
import app.blinkercomma.ui.theme.WatermarkFadeTransition

/**
 * D 화면(주행) — 쉼표 중에만 ⏸ 하나, 레버 방향 쪽에서 1회 슬라이드+페이드로 들어온다(측후방 카메라 폭 780dp
 * 오른쪽 가운데, 설계서 원칙 1·11 · ui_plan.md P0-1·2). 쉼표가 아닐 땐 정적 워터마크만(지속 애니메이션 0, P0-3).
 * 글자 0 · 누를 것 0. 시연 주입 표시는 디버그 빌드에서 주입한 뒤에만(원칙 10).
 */
@Composable
fun DriveScreen(state: ScreenUiState.Drive, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(CommaColors.background)) {
        Box(
            Modifier.fillMaxSize().padding(start = CommaSize.cameraOverlayWidth),
            contentAlignment = Alignment.Center,
        ) {
            WatermarkFadeTransition(visible = !state.isHoldGlyphOn) { CommaWatermark() }
            HoldGlyphTransition(visible = state.isHoldGlyphOn, enterFromLeft = state.holdSide == HoldSide.LEFT) {
                HoldGlyph()
            }
        }
        if (state.isDemoInjected) {
            DemoInjectedTag(Modifier.align(Alignment.BottomEnd).padding(CommaSpace.xlarge))
        }
    }
}
