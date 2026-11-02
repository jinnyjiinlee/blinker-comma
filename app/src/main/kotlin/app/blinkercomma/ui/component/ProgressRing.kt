package app.blinkercomma.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSize

private const val SEGMENT_GAP_DEG = 3f
private const val START_ANGLE_DEG = -90f
private const val FULL_CIRCLE_DEG = 360f

/**
 * P 화면 재생 버튼 둘레의 정적 진행 링 — [current]번째/[total] 문장을 세그먼트로 보여 준다.
 * 연속 회전·Tick마다 리드로잉 없음 — 문장이 바뀔 때만 값이 바뀐다(ui_plan.md P1).
 */
@Composable
fun ProgressRing(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 0) return
    Canvas(modifier) { drawRingSegments(current.coerceIn(0, total), total) }
}

private fun DrawScope.drawRingSegments(filled: Int, total: Int) {
    val sweep = FULL_CIRCLE_DEG / total
    repeat(total) { index -> drawSegment(index, sweep, isFilled = index < filled) }
}

private fun DrawScope.drawSegment(index: Int, sweepDeg: Float, isFilled: Boolean) {
    val color = if (isFilled) CommaColors.signal else CommaColors.ringTrack
    val strokeWidth = CommaSize.progressRingStroke.toPx()
    val diameter = size.minDimension - strokeWidth
    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
    drawArc(
        color = color,
        startAngle = START_ANGLE_DEG + index * sweepDeg + SEGMENT_GAP_DEG / 2f,
        sweepAngle = sweepDeg - SEGMENT_GAP_DEG,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
}
