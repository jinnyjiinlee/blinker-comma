package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaType

/** 오늘 카운트 숫자 하나 + 라벨. [isAccent]면 숫자를 호박색으로(쉼표 수 — 앱이 한 일). 낭독기는 [description] 한 줄로. */
@Composable
fun TallyNumber(
    value: Int,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
) {
    Column(modifier.clearAndSetSemantics { contentDescription = description }) {
        val color = if (isAccent) CommaColors.signal else CommaColors.onSurface
        Text(text = value.toString(), style = CommaType.number, color = color)
        Text(label, style = CommaType.body, color = CommaColors.onSurfaceMuted)
    }
}
