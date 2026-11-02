package app.blinkercomma.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 신호 상태 한 줄 — 빈 원(○ = 못 받음) + 흐린 글자. 경고색을 쓰지 않는다: 앱은 그 신호 없이도 동작한다(원칙 4). */
@Composable
fun StatusLine(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(CommaSpace.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(CommaSize.statusIcon)
                .border(CommaSize.panelBorder, CommaColors.onSurfaceMuted, CommaShape.circle),
        )
        Text(text, style = CommaType.caption, color = CommaColors.onSurfaceMuted)
    }
}
