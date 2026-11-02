package app.blinkercomma.ui.park

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize
import app.blinkercomma.ui.theme.CommaSpace

/**
 * 오늘 쉼표 횟수만큼 점 — 큰 숫자(`TodayTally`)를 설명하는 보조 그래픽(ui_plan.md P0-4).
 * 숫자 쪽에 이미 같은 값이 글자로 있어 장식으로만 두고 접근성 트리에선 뺀다. 0번이면 아무것도 안 그린다(원칙 4 — 지어내지 않음).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayTimeline(commaCount: Int, modifier: Modifier = Modifier) {
    if (commaCount <= 0) return
    FlowRow(
        modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(CommaSpace.small),
    ) {
        repeat(commaCount) { TimelineDot() }
    }
}

@Composable
private fun TimelineDot() {
    Box(Modifier.size(CommaSize.timelineDot).background(CommaColors.signal, CircleShape))
}
