package app.blinkercomma.ui.park

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.component.StepRow
import app.blinkercomma.ui.theme.CommaSpace

/** 사용법 세 줄(레버 → 쉼표 → 재개) — 처음 연 사람이 정차 중에 앱이 하는 일을 한 번에 알게. P 화면에서만. */
@Composable
fun HowItWorks(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(CommaSpace.medium)) {
        StepRow(
            R.drawable.ic_turn_signal,
            stringResource(R.string.step_lever_title),
            stringResource(R.string.step_lever_detail),
        )
        StepRow(
            R.drawable.ic_pause,
            stringResource(R.string.step_hold_title),
            stringResource(R.string.step_hold_detail),
        )
        StepRow(
            R.drawable.ic_play,
            stringResource(R.string.step_resume_title),
            stringResource(R.string.step_resume_detail),
        )
    }
}
