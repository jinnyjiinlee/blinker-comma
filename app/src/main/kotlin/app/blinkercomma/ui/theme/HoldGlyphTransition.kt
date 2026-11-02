package app.blinkercomma.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

private const val ENTER_MS = 260
private const val EXIT_MS = 180

/**
 * ⏸의 1회성 전환 — 지속 루프 금지(ui_plan.md 4절, 애니메이션 0 원칙을 "지속 루프 0"으로 재해석, 대표 승인 11/2 20시).
 * 등장: 레버 방향 쪽 가장자리에서 슬라이드+페이드로 들어와 같은 중앙 자리에 정착. 퇴장: 이동 없이 페이드만.
 */
@Composable
fun HoldGlyphTransition(visible: Boolean, enterFromLeft: Boolean, content: @Composable () -> Unit) {
    val offsetPx = with(LocalDensity.current) { CommaSize.holdGlyphEnterOffset.roundToPx() }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(ENTER_MS)) +
            slideInHorizontally(tween(ENTER_MS)) { if (enterFromLeft) -offsetPx else offsetPx },
        exit = fadeOut(tween(EXIT_MS)),
        content = { content() },
    )
}
