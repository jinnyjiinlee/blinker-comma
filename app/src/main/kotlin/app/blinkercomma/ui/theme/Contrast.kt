package app.blinkercomma.ui.theme

import kotlin.math.pow

private const val CHANNEL_MAX = 255.0
private const val LINEAR_THRESHOLD = 0.03928
private const val LINEAR_DIVISOR = 12.92
private const val GAMMA_OFFSET = 0.055
private const val GAMMA = 2.4
private const val FLARE = 0.05
private const val RED_WEIGHT = 0.2126
private const val GREEN_WEIGHT = 0.7152
private const val BLUE_WEIGHT = 0.0722
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MASK = 0xFFL

/**
 * WCAG 상대 휘도 대비(1~21). 운전 화면 하한은 4.5(Android for Cars 디자인 가이드).
 * [flare]: 두 색에 똑같이 더해지는 빛 — 기본은 WCAG 0.05, 햇빛 반사를 보려면 더 크게(밝을수록 어두운 테마의 대비가 줄어듦).
 */
fun calcContrastRatio(foreground: Long, background: Long, flare: Double = FLARE): Double {
    val (lighter, darker) = listOf(luminance(foreground), luminance(background)).sortedDescending()
    return (lighter + flare) / (darker + flare)
}

private fun luminance(argb: Long): Double =
    RED_WEIGHT * linear(argb, RED_SHIFT) + GREEN_WEIGHT * linear(argb, GREEN_SHIFT) + BLUE_WEIGHT * linear(argb, 0)

private fun linear(argb: Long, shift: Int): Double {
    val channel = ((argb shr shift) and CHANNEL_MASK) / CHANNEL_MAX
    return if (channel <= LINEAR_THRESHOLD) {
        channel / LINEAR_DIVISOR
    } else {
        ((channel + GAMMA_OFFSET) / (1 + GAMMA_OFFSET)).pow(
            GAMMA,
        )
    }
}
