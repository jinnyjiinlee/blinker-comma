package app.blinkercomma.ui.park

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.component.StepRow
import app.blinkercomma.ui.theme.CommaSpace

/**
 * 사용법 세 줄(레버 → 쉼표 → 재개) — 처음 연 사람이 정차 중에 앱이 하는 일을 한 번에 알게. P 화면에서만.
 * 3번 줄은 자동 재개 설정을 따른다(끔이면 핸들 재생 버튼으로 재개).
 */
@Composable
fun HowItWorks(isAutoResume: Boolean, modifier: Modifier = Modifier) {
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
            stringResource(resumeStepTitle(isAutoResume)),
            stringResource(R.string.step_resume_detail),
        )
    }
}

/** 3번 줄 제목 — 자동 재개 켬이면 레버, 끔(기본값)이면 핸들 재생 버튼. */
internal fun resumeStepTitle(isAutoResume: Boolean): Int =
    if (isAutoResume) R.string.step_resume_title else R.string.step_resume_manual_title
