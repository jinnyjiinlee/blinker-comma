package app.blinkercomma.ui.park

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.ScreenUiState
import app.blinkercomma.ui.component.BrandHeader
import app.blinkercomma.ui.component.SettingToggleRow
import app.blinkercomma.ui.component.ToggleRowText
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/**
 * P 화면(정차) — 왼쪽: 표지 · 사용법 · 오늘 카운트 · 자동 재개 · 출처 / 오른쪽 판: 팩 제목 · 재생 버튼 · 지금 단계 · 신호 상태.
 * 누르는 것(재생·토글)은 측후방 카메라 자리(왼쪽 위 780×440dp) 밖에만 둔다.
 */
@Composable
fun ParkScreen(state: ScreenUiState.Park, actions: ParkActions, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxSize().background(CommaColors.background).padding(CommaSpace.xlarge),
        horizontalArrangement = Arrangement.spacedBy(CommaSpace.xlarge),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(CommaSpace.large)) {
            BrandHeader()
            HowItWorks(Modifier.padding(top = CommaSpace.large))
            Spacer(Modifier.weight(1f))
            TodayTally(state.todaySentences, state.todayCommas)
            SettingToggleRow(autoResumeText(state.isAutoResume), state.isAutoResume, actions.onAutoResumeToggle)
            if (state.credit.isNotBlank()) CreditLine(state.credit)
        }
        PlaybackPanel(state, actions.onPlayToggle, Modifier.width(CommaSize.sidePanel).fillMaxHeight())
    }
}

/** P 화면에서 누를 수 있는 것 전부. */
data class ParkActions(val onPlayToggle: () -> Unit, val onAutoResumeToggle: () -> Unit)

@Composable
private fun autoResumeText(isOn: Boolean) = ToggleRowText(
    title = stringResource(R.string.auto_resume_title),
    detail = stringResource(if (isOn) R.string.auto_resume_on_detail else R.string.auto_resume_off_detail),
    state = stringResource(if (isOn) R.string.toggle_state_on else R.string.toggle_state_off),
)

@Composable
private fun CreditLine(credit: String) {
    Text(
        text = stringResource(R.string.credit_label, credit),
        style = CommaType.caption,
        color = CommaColors.onSurfaceMuted,
        modifier = Modifier.fillMaxWidth(),
    )
}
