package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 앱 표지 — 호박색 쉼표 + 앱 이름 + 스토어 한 줄. 런처 아이콘과 같은 쉼표라 앱을 열어도 같은 얼굴. */
@Composable
fun BrandHeader(modifier: Modifier = Modifier, isTaglineShown: Boolean = true) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(CommaSpace.small)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CommaSpace.small),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comma_mark),
                contentDescription = null,
                tint = CommaColors.signal,
                modifier = Modifier.size(CommaSize.brandMark),
            )
            Text(stringResource(R.string.app_name), style = CommaType.title, color = CommaColors.onSurface)
        }
        if (isTaglineShown) {
            Text(stringResource(R.string.tagline), style = CommaType.body, color = CommaColors.onSurfaceMuted)
        }
    }
}
