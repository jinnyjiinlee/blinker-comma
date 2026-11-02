package app.blinkercomma.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.blinkercomma.ui.HoldSide
import app.blinkercomma.ui.ScreenUiState
import app.blinkercomma.ui.component.CommaWatermark
import app.blinkercomma.ui.component.DemoInjectedTag
import app.blinkercomma.ui.component.HoldCountBadge
import app.blinkercomma.ui.component.HoldGlyph
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.HoldCountBadgeTransition
import app.blinkercomma.ui.theme.HoldGlyphTransition
import app.blinkercomma.ui.theme.WatermarkFadeTransition

/**
 * D 화면(주행) — 쉼표 중에만 ⏸ 하나 + 「쉼표 N」 배지, 레버 방향 쪽에서 1회 슬라이드+페이드로 들어온다(측후방
 * 카메라 폭 780dp 오른쪽 가운데, 설계서 원칙 1·11 · ui_plan.md P0-1·2). 쉼표가 아닐 땐 정적 워터마크만(지속
 * 애니메이션 0, P0-3). 글자는 「쉼표 N」 배지 하나뿐(M8 upgrade_plan 4-2, 원칙 1의 유일한 의도적 예외) · 누를
 * 것 0. 카메라 자리 경계선은 정적 장식(UJ 공통 지적 2 — "겹쳐 보인다"는 인상 완화). 시연 주입 표시는 디버그
 * 빌드에서 주입한 뒤에만(원칙 10).
 */
@Composable
fun DriveScreen(state: ScreenUiState.Drive, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(CommaColors.background)) {
        CameraBoundaryLine(Modifier.align(Alignment.CenterStart).padding(start = CommaSize.cameraOverlayWidth))
        Box(
            Modifier.fillMaxSize().padding(start = CommaSize.cameraOverlayWidth, end = CommaSpace.large),
            contentAlignment = Alignment.Center,
        ) {
            WatermarkFadeTransition(visible = !state.isHoldGlyphOn) { CommaWatermark() }
            HoldGlyphTransition(visible = state.isHoldGlyphOn, enterFromLeft = state.holdSide == HoldSide.LEFT) {
                HoldGlyph()
            }
            HoldCountBadgeTransition(
                visible = state.isHoldGlyphOn,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = CommaSpace.medium),
            ) { HoldCountBadge(state.todayCommas) }
        }
        if (state.isDemoInjected) {
            DemoInjectedTag(Modifier.align(Alignment.BottomEnd).padding(CommaSpace.xlarge))
        }
    }
}

/** 측후방 카메라 자리와 앱 요소 사이의 아주 옅은 경계선 — 정적·장식, "겹쳐 보인다"는 인상만 완화(UJ 공통 지적 2). */
@Composable
private fun CameraBoundaryLine(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxHeight().width(CommaSize.panelBorder).background(CommaColors.ringTrack))
}
