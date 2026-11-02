package app.blinkercomma.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaType

/**
 * D 화면 ⏸ 옆 「쉼표 N」 — 오늘 쉼표 횟수, P 화면 숫자와 같은 호박색(M8 upgrade_plan 4-2).
 * 원칙 1(글자 0)의 유일한 의도적 예외 — 단어 하나 + 숫자 한 자리로 한 글랜스 안에 읽힌다.
 */
@Composable
fun HoldCountBadge(count: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.hold_count_badge, count),
        style = CommaType.title,
        color = CommaColors.signal,
        modifier = modifier,
    )
}
