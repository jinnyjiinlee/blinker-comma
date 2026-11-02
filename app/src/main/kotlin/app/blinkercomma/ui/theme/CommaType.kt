package app.blinkercomma.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

/**
 * 글자 토큰. 가장 작은 글자도 24sp(Android for Cars 최소 글자) — `CommaThemeFloorTest`가 막는다.
 * 앱 창이 1682×1210dp(160dpi, 에뮬레이터 실측 — 진행 기록 D3)라 폰 크기 글자는 흘끗 봐서 안 읽힌다.
 * 큰 글자는 구(句) 단위로 끊어 마지막 줄에 한 단어만 남지 않게 한다(API 33+).
 */
object CommaType {
    const val NUMBER_SP = 132f
    const val TITLE_SP = 52f
    const val ACTION_SP = 40f
    const val BODY_SP = 34f
    const val CAPTION_SP = 28f

    /** 오늘 카운트 숫자 — 문장 없이 숫자만 크게(그래프·점수 대신). */
    val number = TextStyle(fontSize = NUMBER_SP.sp, fontWeight = FontWeight.Bold, lineHeight = 140.sp)
    val title = TextStyle(
        fontSize = TITLE_SP.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 64.sp,
        lineBreak = LineBreak.Heading,
    )
    val action = TextStyle(fontSize = ACTION_SP.sp, fontWeight = FontWeight.Bold, lineHeight = 48.sp)
    val body = TextStyle(
        fontSize = BODY_SP.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 46.sp,
        lineBreak = LineBreak.Heading,
    )
    val bodyStrong = body.copy(fontWeight = FontWeight.Bold)
    val caption = TextStyle(fontSize = CAPTION_SP.sp, fontWeight = FontWeight.Medium, lineHeight = 38.sp)

    /** 하한 테스트용 — 새 글자 토큰을 만들면 여기에도. */
    val allSizesSp: List<Float> = listOf(NUMBER_SP, TITLE_SP, ACTION_SP, BODY_SP, CAPTION_SP)
}
