package app.blinkercomma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize

/** 임시 P 화면(D2 실측용) — ▶/❙❙ · 오늘 카운트 · 자동 재개 · ⏸ · 시연 주입 표시. 정식 화면은 D3. */
@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onPlayToggle: () -> Unit,
    onAutoResumeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().background(CommaColors.background),
        verticalArrangement = Arrangement.spacedBy(CommaSize.gap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val play = if (state.isPlaying) R.string.action_pause else R.string.action_play
        TextButton(onClick = onPlayToggle) { ScreenText(stringResource(play), CommaSize.playGlyph) }
        ScreenText(stringResource(R.string.tally_today, state.sentences, state.commas), CommaSize.number)
        val resume = if (state.isAutoResume) R.string.auto_resume_on else R.string.auto_resume_off
        TextButton(onClick = onAutoResumeToggle) { ScreenText(stringResource(resume), CommaSize.label) }
        if (state.isHoldGlyphOn) ScreenText(stringResource(R.string.hold_glyph), CommaSize.holdGlyph)
        if (state.isDemoInjected) ScreenText(stringResource(R.string.demo_injected), CommaSize.label, isDemo = true)
    }
}

@Composable
private fun ScreenText(text: String, size: TextUnit, isDemo: Boolean = false) {
    Text(text, color = if (isDemo) CommaColors.demoInjected else CommaColors.text, fontSize = size)
}
