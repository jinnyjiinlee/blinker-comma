package app.blinkercomma.engine.comma

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** 쉼표 문턱값. 숫자 근거는 설계서 「쉼표 규칙」 표. 「자동 재개」는 P 설정이라 앱이 `copy`로 바꿔 넘긴다. */
data class CommaCardParams(
    /** R5 레버 OFF가 이만큼 유지돼야 재개 — 결선 질의응답 「OFF 3.7 + 0.8 = 4.5」. 스위치 값으로 확인되면 0. */
    val offDebounce: Duration = 0.8.seconds,
    /** R4 예약 뒤 문장이 이 안에 안 끝나면 문장 중간에서 멈춤 — 카드 쉼표 규칙 2. */
    val endWaitLimit: Duration = 2.5.seconds,
    /** R1 「이동 중」 = 이 속도 초과. VC 최소 1km/h(0.28m/s)보다 위로 둔 설계값. */
    val movingMinMps: Double = 0.3,
    /** R8 끄면 레버 OFF로 재개하지 않고 핸들 키·P의 ▶로만. */
    val isAutoResume: Boolean = true,
) {
    companion object {
        val DEFAULT = CommaCardParams()
    }
}
