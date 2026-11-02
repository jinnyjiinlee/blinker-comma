package app.blinkercomma.ui.theme

import androidx.compose.ui.unit.dp

/** 터치 타깃·아이콘 크기. 하한 숫자는 Android for Cars 디자인 가이드 sizing 값. */
object CommaSize {
    const val TOUCH_TARGET_DP = 76f

    /** 버튼·설정 줄 높이 — 하한 76dp는 바닥일 뿐, 1210dp 높이 창에선 가는 띠로 보여 120dp로(창 높이 약 1/10). */
    const val BUTTON_DP = 120f

    /** 재생 버튼 지름 — P 화면의 주인공. 창 높이의 약 1/5. */
    const val PLAY_BUTTON_DP = 232f

    /** D 화면 쉼표 표시 ⏸ — 카드 「D: ⏸ 96dp 요소 1」. */
    const val HOLD_GLYPH_DP = 96f

    val button = BUTTON_DP.dp
    val playButton = PLAY_BUTTON_DP.dp
    val playIcon = 104.dp
    val holdGlyph = HOLD_GLYPH_DP.dp
    val brandMark = 64.dp
    val stepBadge = 72.dp
    val stepIcon = 40.dp
    val statusIcon = 24.dp
    val panelBorder = 2.dp

    /** 그림 버튼(재생 등)의 최소 폭 — 설정 줄 글자 칸이 줄어도 이 아래로는 안 줄인다. */
    val sidePanel = 560.dp

    /**
     * 깜빡이 때 시스템이 앱 창 왼쪽 위에 띄우는 측후방 카메라(setup T_results 「약 750px」, 이전 출품작 캡처 실측
     * 780×425dp + 여유). ⏸는 이 폭 오른쪽에만(설계서 원칙 11).
     */
    val cameraOverlayWidth = 780.dp
    val cameraOverlayHeight = 440.dp
}
