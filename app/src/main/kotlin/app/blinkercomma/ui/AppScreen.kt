package app.blinkercomma.ui

import androidx.compose.runtime.Composable
import app.blinkercomma.ui.drive.DriveScreen
import app.blinkercomma.ui.park.ParkActions
import app.blinkercomma.ui.park.ParkScreen
import app.blinkercomma.ui.permission.PermissionScreen
import app.blinkercomma.ui.theme.CommaTheme

/** 화면 고르기만 — 어느 화면인지는 [toUiState]가 정했다. */
@Composable
fun AppScreen(state: ScreenUiState, actions: ParkActions, onAllowSpeed: () -> Unit) {
    CommaTheme {
        when (state) {
            is ScreenUiState.Park -> ParkScreen(state, actions)
            is ScreenUiState.Drive -> DriveScreen(state)
            ScreenUiState.AskSpeedPermission -> PermissionScreen(onAllowSpeed)
        }
    }
}
