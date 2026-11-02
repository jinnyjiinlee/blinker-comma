package app.blinkercomma.ui.park

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.MissingSignal
import app.blinkercomma.ui.Playback
import app.blinkercomma.ui.ScreenUiState
import app.blinkercomma.ui.component.DemoInjectedTag
import app.blinkercomma.ui.component.RoundPlayButton
import app.blinkercomma.ui.component.StatusLine
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** P 화면 오른쪽 판 — 팩 제목(위) · 재생 버튼과 지금 단계(가운데) · 못 받은 신호·시연 주입(아래). */
@Composable
fun PlaybackPanel(state: ScreenUiState.Park, onPlayToggle: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.background(CommaColors.surface, CommaShape.panel).padding(CommaSpace.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CommaSpace.medium),
    ) {
        Text(stringResource(R.string.now_reading), style = CommaType.caption, color = CommaColors.onSurfaceMuted)
        Text(state.packTitle, style = CommaType.title, color = CommaColors.onSurface)
        Spacer(Modifier.weight(1f))
        RoundPlayButton(isPlaying = state.playback == Playback.READING, onClick = onPlayToggle)
        Text(stringResource(state.playback.label()), style = CommaType.title, color = CommaColors.onSurface)
        if (state.sentenceCount > 0) {
            val progress = stringResource(R.string.sentence_progress, state.sentenceNumber, state.sentenceCount)
            Text(progress, style = CommaType.body, color = CommaColors.onSurfaceMuted)
        }
        Spacer(Modifier.weight(1f))
        state.missingSignals.forEach { StatusLine(stringResource(it.label()), Modifier.align(Alignment.Start)) }
        if (state.isDemoInjected) DemoInjectedTag()
    }
}

private fun Playback.label(): Int = when (this) {
    Playback.STOPPED -> R.string.phase_stopped
    Playback.READING -> R.string.phase_reading
    Playback.HOLDING -> R.string.phase_holding
    Playback.FOCUS_PAUSED -> R.string.phase_focus_paused
}

private fun MissingSignal.label(): Int = when (this) {
    MissingSignal.GEAR -> R.string.signal_missing_gear
    MissingSignal.SPEED -> R.string.signal_missing_speed
}
