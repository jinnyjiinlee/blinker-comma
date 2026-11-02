package app.blinkercomma.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaShape
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaType

/** 글자 버튼은 전부 이것 — 높이 120dp(하한 76dp 위)·호박색 채움을 한 곳에서. 폭은 쓰는 쪽이 정한다. */
@Composable
fun BigButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = CommaSize.button),
        shape = CommaShape.button,
        colors = ButtonDefaults.buttonColors(containerColor = CommaColors.signal, contentColor = CommaColors.onSignal),
    ) {
        Text(text = label, style = CommaType.action)
    }
}
