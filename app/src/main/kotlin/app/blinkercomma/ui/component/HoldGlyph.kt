package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize

/** D 화면의 유일한 요소 — 쉼표 중 ⏸ 96dp(호박색). 움직이지 않는다(애니메이션 0). */
@Composable
fun HoldGlyph(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_hold),
        contentDescription = stringResource(R.string.hold_glyph_description),
        tint = CommaColors.signal,
        modifier = modifier.size(CommaSize.holdGlyph),
    )
}
