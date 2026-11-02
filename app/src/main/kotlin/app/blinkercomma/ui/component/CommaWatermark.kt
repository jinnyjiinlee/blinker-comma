package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize

/** D 화면이 비어 보이지 않게 — 쉼표가 아닐 때만 아주 옅게(ui_plan.md P0-3). 정적, 장식이라 설명 0. */
@Composable
fun CommaWatermark(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_comma_mark),
        contentDescription = null,
        tint = CommaColors.signalDim,
        modifier = modifier.size(CommaSize.watermarkCommaSize),
    )
}
