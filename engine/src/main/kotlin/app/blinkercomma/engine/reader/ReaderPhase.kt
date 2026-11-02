package app.blinkercomma.engine.reader

import kotlin.time.Duration

/** 낭독 단계. 쉼표 예약·디바운스 시각은 그 단계에서만 뜻이 있어 단계 안에 둔다. */
sealed interface ReaderPhase {
    /** 재생 안 함. 다음 재생은 지금 문장 처음부터. 앱 실행 직후도 여기(자동 재생 금지 — 설계서 원칙 5). */
    data object Stopped : ReaderPhase

    /** 낭독 중. `armedAt` = 쉼표 예약 순간(R1), null = 예약 없음. */
    data class Reading(val armedAt: Duration? = null) : ReaderPhase

    /** 쉼표 중(⏸). `offSince` = 레버가 내려온 순간 — R5 디바운스 시작, null = 레버가 아직 올라가 있음. */
    data class Holding(val reason: HoldReason, val offSince: Duration? = null) : ReaderPhase

    /** 다른 앱이 포커스를 잠시 가져가 멈춤(R10). 돌려받으면 멈춘 자리부터. */
    data object FocusPaused : ReaderPhase
}

enum class HoldReason {
    /** 문장 끝에서 멈춤 — 재개하면 다음 문장. */
    SENTENCE_END,

    /** 예약 뒤 상한 안에 문장이 안 끝나 중간에서 멈춤(R4) — 재개하면 같은 문장 처음부터. */
    MID_SENTENCE,
}
