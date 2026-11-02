package app.blinkercomma.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** 앱 루트에서 한 번 감싼다. Material 기본 글자 슬롯도 24sp 이상으로 덮어 기본값이 새지 않게 한다. */
@Composable
fun CommaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = commaColorScheme, typography = commaTypography, content = content)
}

private val commaColorScheme = darkColorScheme(
    primary = CommaColors.signal,
    onPrimary = CommaColors.onSignal,
    background = CommaColors.background,
    onBackground = CommaColors.onSurface,
    surface = CommaColors.surface,
    onSurface = CommaColors.onSurface,
)

private val commaTypography = Typography(
    displayLarge = CommaType.number, displayMedium = CommaType.title, displaySmall = CommaType.title,
    headlineLarge = CommaType.title, headlineMedium = CommaType.title, headlineSmall = CommaType.title,
    titleLarge = CommaType.title, titleMedium = CommaType.body, titleSmall = CommaType.body,
    bodyLarge = CommaType.body, bodyMedium = CommaType.body, bodySmall = CommaType.caption,
    labelLarge = CommaType.action, labelMedium = CommaType.caption, labelSmall = CommaType.caption,
)
