package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize

/** P 화면의 주인공 — 호박색 원 하나, 재생 중이면 정지 막대. 글자 대신 그림이라 이름은 화면 낭독기용으로만. */
@Composable
fun RoundPlayButton(isPlaying: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
    val label = if (isPlaying) R.string.action_stop else R.string.action_play
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(CommaSize.playButton),
        shape = CommaShape.circle,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = CommaColors.signal,
            contentColor = CommaColors.onSignal,
        ),
    ) {
        Icon(painterResource(icon), stringResource(label), Modifier.size(CommaSize.playIcon))
    }
}
