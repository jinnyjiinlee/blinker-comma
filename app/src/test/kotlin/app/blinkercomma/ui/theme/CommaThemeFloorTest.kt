package app.blinkercomma.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

/** Android for Cars 디자인 가이드 하한 — 글자 24sp · 대비 4.5:1(글자 아닌 표시 3:1) · 터치 76dp. 운전 중 화면 규칙을 숫자로 못 박는다. */
class CommaThemeFloorTest {
    @Test
    fun `모든 글자색은 바탕과 대비 4점5 이상이다`() {
        val weakPairs = CommaColors.contrastPairs.filter { (text, bg) -> calcContrastRatio(text, bg) < MIN_CONTRAST }
        assertEquals(emptyList(), weakPairs)
    }

    @Test
    fun `햇빛 반사가 더해져도 글자 대비는 3 이상이다`() {
        val weakPairs = CommaColors.contrastPairs.filter { (text, bg) ->
            calcContrastRatio(text, bg, flare = SUNLIGHT_FLARE) < MIN_SUNLIGHT_CONTRAST
        }
        assertEquals(emptyList(), weakPairs)
    }

    @Test
    fun `글자 아닌 표시는 옆 바탕과 대비 3 이상이다`() {
        val weak = CommaColors.indicatorPairs.filter { (mark, bg) ->
            calcContrastRatio(mark, bg) < MIN_INDICATOR_CONTRAST
        }
        assertEquals(emptyList(), weak)
    }

    @Test
    fun `가장 작은 글자도 24sp 이상이다`() {
        assertEquals(emptyList(), TEXT_SIZES_SP.filter { it < MIN_TEXT_SP })
    }

    @Test
    fun `터치 타깃은 76dp 이상이다`() {
        assertEquals(true, CommaSize.TOUCH_TARGET_DP >= MIN_TOUCH_DP)
    }

    @Test
    fun `버튼 높이는 터치 타깃 하한 이상이다`() {
        assertEquals(true, CommaSize.BUTTON_DP >= MIN_TOUCH_DP)
    }

    @Test
    fun `재생 버튼은 터치 타깃 하한 이상이다`() {
        assertEquals(true, CommaSize.PLAY_BUTTON_DP >= MIN_TOUCH_DP)
    }

    @Test
    fun `쉼표 표시는 카드의 96dp다`() {
        assertEquals(96f, CommaSize.HOLD_GLYPH_DP)
    }

    @Test
    fun `쉼표 진입 애니메이션은 카메라 자리를 넘지 않는다`() {
        val contentWidthDp = APP_WIDTH_DP - CommaSize.cameraOverlayWidth.value
        val worstCaseLeftDp = contentWidthDp / 2 -
            CommaSize.holdGlyphEnterOffset.value - CommaSize.HOLD_GLYPH_DP / 2
        assertEquals(true, worstCaseLeftDp >= 0f)
    }

    @Test
    fun `흰 글자와 검은 바탕의 대비는 21이다`() {
        assertEquals(21.0, calcContrastRatio(0xFFFFFFFF, 0xFF000000), absoluteTolerance = 0.01)
    }

    private companion object {
        const val MIN_CONTRAST = 4.5
        const val MIN_INDICATOR_CONTRAST = 3.0

        // 주간 대비 모델(가정): 맑은 날 화면 반사를 두 색에 휘도 0.25씩 더함 — 어두운 테마는 이때 대비가 가장 많이 줄어듦.
        // 하한 3 = WCAG 큰 글자(18pt 굵게 이상) 하한 — 이 앱 글자는 가장 작은 것도 24sp
        const val SUNLIGHT_FLARE = 0.25
        const val MIN_SUNLIGHT_CONTRAST = 3.0
        const val MIN_TEXT_SP = 24f
        const val MIN_TOUCH_DP = 76f

        // 앱 창 폭 — D3 dumpsys window 실측(ARCHITECTURE.md 8절), 코드엔 토큰 없어 여기서만 상수로.
        const val APP_WIDTH_DP = 1682f
        val TEXT_SIZES_SP = CommaType.allSizesSp
    }
}
