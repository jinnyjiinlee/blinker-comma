package app.blinkercomma.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 스위치 그림 배율 — Material 스위치(52×32dp)는 차 화면에서 작다. 누르는 곳은 줄 전체(120dp)라 그림만 키운다. */
private const val SWITCH_SCALE = 1.5f

/** 켬·끔 설정 한 줄 — 줄 전체가 누르는 곳. 상태는 스위치 색 + 「켬/끔」 글자로 이중 표시(색각). */
@Composable
fun SettingToggleRow(row: ToggleRowText, isOn: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = CommaSize.button)
            .background(CommaColors.surface, CommaShape.panel)
            .toggleable(value = isOn, role = Role.Switch, onValueChange = { onToggle() })
            .padding(horizontal = CommaSpace.large, vertical = CommaSpace.medium),
        horizontalArrangement = Arrangement.spacedBy(CommaSpace.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.title, style = CommaType.bodyStrong, color = CommaColors.onSurface)
            Text(row.detail, style = CommaType.caption, color = CommaColors.onSurfaceMuted)
        }
        Text(
            row.state,
            style = CommaType.bodyStrong,
            color = if (isOn) CommaColors.signal else CommaColors.onSurfaceMuted,
        )
        Switch(checked = isOn, onCheckedChange = null, modifier = Modifier.scale(SWITCH_SCALE), colors = switchColors())
    }
}

/** 줄에 보이는 글자 셋 — 화면이 stringResource로 골라 넘긴다. */
data class ToggleRowText(val title: String, val detail: String, val state: String)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = CommaColors.onSignal,
    checkedTrackColor = CommaColors.signal,
    uncheckedThumbColor = CommaColors.onSurfaceMuted,
    uncheckedTrackColor = CommaColors.background,
    uncheckedBorderColor = CommaColors.onSurfaceMuted,
)
