package app.blinkercomma.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 사용법 한 줄 — 호박색 그림(원 안) + 굵은 말 + 흐린 덧말. 그림은 장식이라 낭독기는 글자만 읽는다. */
@Composable
fun StepRow(@DrawableRes icon: Int, title: String, detail: String, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(CommaSpace.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(CommaSize.stepBadge).background(CommaColors.surface, CommaShape.circle)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = CommaColors.signal,
                modifier = Modifier.size(CommaSize.stepIcon).align(Alignment.Center),
            )
        }
        Text(title, style = CommaType.bodyStrong, color = CommaColors.onSurface)
        Text(detail, style = CommaType.body, color = CommaColors.onSurfaceMuted)
    }
}
