package app.blinkercomma.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.component.BigButton
import app.blinkercomma.ui.component.BrandHeader
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/**
 * 첫 실행 속도 권한 안내(정차에서만) — 왜 필요한지 한 문장 + [허용] 하나. 시스템 창에서 거부해도 앱은 레버 + D로 동작하고
 * 다시 묻지 않는다(원칙 4). 버튼은 P 화면 재생 버튼과 같은 오른쪽 폭에.
 */
@Composable
fun PermissionScreen(onAllow: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(CommaColors.background).padding(CommaSpace.xlarge),
        verticalArrangement = Arrangement.spacedBy(CommaSpace.medium),
    ) {
        BrandHeader(isTaglineShown = false)
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.permission_line), style = CommaType.title, color = CommaColors.onSurface)
        Text(stringResource(R.string.permission_detail), style = CommaType.body, color = CommaColors.onSurfaceMuted)
        Text(
            stringResource(R.string.permission_system_note),
            style = CommaType.body,
            color = CommaColors.onSurfaceMuted,
        )
        Spacer(Modifier.weight(1f))
        BigButton(
            label = stringResource(R.string.permission_allow),
            onClick = onAllow,
            modifier = Modifier.width(CommaSize.sidePanel).align(Alignment.End),
        )
    }
}
