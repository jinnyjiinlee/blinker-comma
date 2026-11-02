package app.blinkercomma.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
 * 등장: 레버 방향 쪽 가장자리에서 슬라이드+페이드로 들어와 같은 중앙 자리에 정착(FastOutSlowIn — 급가속 없이 부드럽게 멈춤).
 * 퇴장: 이동 없이 페이드만(FastOutLinearIn — ui_plan.md 4절 명세값, U2에서 default easing 대신 명시로 바꿈).
 */
@Composable
fun HoldGlyphTransition(visible: Boolean, enterFromLeft: Boolean, content: @Composable () -> Unit) {
    val offsetPx = with(LocalDensity.current) { CommaSize.holdGlyphEnterOffset.roundToPx() }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(ENTER_MS, easing = FastOutSlowInEasing)) +
            slideInHorizontally(tween(ENTER_MS, easing = FastOutSlowInEasing)) {
                if (enterFromLeft) -offsetPx else offsetPx
            },
        exit = fadeOut(tween(EXIT_MS, easing = FastOutLinearInEasing)),
        content = { content() },
    )
}

/**
 * D 워터마크의 1회성 페이드 — ⏸ 전환과 같은 시간(등장 260ms·퇴장 180ms)으로 교차해 「하나의 화면」처럼 보이게(U2 다듬기).
 * 워터마크는 장식이라 이동 없이 페이드만.
 */
@Composable
fun WatermarkFadeTransition(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(ENTER_MS, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(EXIT_MS, easing = FastOutLinearInEasing)),
        content = { content() },
    )
}
