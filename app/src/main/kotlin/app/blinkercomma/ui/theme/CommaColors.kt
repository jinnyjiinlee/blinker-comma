package app.blinkercomma.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 색 토큰 — 이름은 쓰임새로. 값은 ARGB `Long`으로 한 번만 적고 `Color`로 감싸 내보낸다
 * (대비 테스트가 Compose 없이 `Long`만 보고 돈다). 어두운 차 화면 기준 고대비(설계서 화면 목록 「어두운 테마 기본」).
 */
object CommaColors {
    const val BACKGROUND: Long = 0xFF0D0F12
    const val SURFACE: Long = 0xFF1A1D22
    const val ON_SURFACE: Long = 0xFFF4F5F7

    /** 보조 글자 — 햇빛 반사 모델에서도 판 위 3 이상이 되게 밝게(0xFFA9B0BA는 2.6이라 버림). */
    const val ON_SURFACE_MUTED: Long = 0xFFC2C8D0

    /** 깜빡이 호박색 — 앱의 한 가지 강조색(쉼표 ⏸·재생 버튼·로고). 0xFFFFA726은 햇빛 모델 2.9라 한 단계 밝게. */
    const val SIGNAL: Long = 0xFFFFB74D
    const val ON_SIGNAL: Long = 0xFF0D0F12

    /** 시연 주입 표시(디버그 빌드) — 호박색과 헷갈리지 않게 레몬색 + 테두리. */
    const val DEMO_INJECTED: Long = 0xFFFFEB66

    /** 워터마크 불투명도 — U3: UJ 블라인드 심사 공통 지적 1(「거의 안 보일 정도로 옅다」) 반영, 12%→20%. */
    const val WATERMARK_ALPHA: Float = 0.20f

    /** 진행 링의 못 채운 세그먼트 불투명도 — 워터마크보다 또렷하되 숫자·글자보다는 약하게. */
    const val RING_TRACK_ALPHA: Float = 0.22f

    val background = Color(BACKGROUND)
    val surface = Color(SURFACE)
    val onSurface = Color(ON_SURFACE)
    val onSurfaceMuted = Color(ON_SURFACE_MUTED)
    val signal = Color(SIGNAL)
    val onSignal = Color(ON_SIGNAL)
    val demoInjected = Color(DEMO_INJECTED)

    /** D 화면 워터마크(읽는 중 정적 표시)만 — 정보 전달용이 아니라 장식이라 대비 하한 대상 아님(ui_plan.md 7절). */
    val signalDim = signal.copy(alpha = WATERMARK_ALPHA)

    /** 진행 링 트랙 — 숫자(n/전체)로 이미 전달되는 정보의 보조 그래픽이라 대비 하한 대상 아님(signalDim과 같은 근거). */
    val ringTrack = onSurfaceMuted.copy(alpha = RING_TRACK_ALPHA)

    /** (글자·아이콘 색, 그 바탕) — 새 색을 추가하면 여기에도. `CommaThemeFloorTest`가 대비 4.5 이상을 본다. */
    val contrastPairs: List<Pair<Long, Long>> = listOf(
        ON_SURFACE to BACKGROUND,
        ON_SURFACE to SURFACE,
        ON_SURFACE_MUTED to BACKGROUND,
        ON_SURFACE_MUTED to SURFACE,
        SIGNAL to BACKGROUND,
        SIGNAL to SURFACE,
        ON_SIGNAL to SIGNAL,
        DEMO_INJECTED to BACKGROUND,
    )

    /** 글자가 아닌 표시(스위치 손잡이·판 테두리) × 그 옆 바탕 — 대비 3 이상(WCAG 1.4.11 비텍스트). */
    val indicatorPairs: List<Pair<Long, Long>> = listOf(
        SIGNAL to BACKGROUND,
        SIGNAL to SURFACE,
        ON_SURFACE_MUTED to SURFACE,
        ON_SIGNAL to SIGNAL,
    )
}
