package app.blinkercomma.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 「시연 주입」 표시(설계서 원칙 10) — 디버그 빌드에서 VC로 못 넣는 신호를 넣은 뒤에만. 릴리스엔 안 뜬다. */
@Composable
fun DemoInjectedTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.demo_injected),
        style = CommaType.caption,
        color = CommaColors.demoInjected,
        modifier = modifier
            .border(CommaSize.panelBorder, CommaColors.demoInjected, CommaShape.button)
            .padding(horizontal = CommaSpace.large, vertical = CommaSpace.small),
    )
}
