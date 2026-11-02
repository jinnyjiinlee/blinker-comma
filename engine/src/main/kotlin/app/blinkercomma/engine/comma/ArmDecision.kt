package app.blinkercomma.engine.comma

/** 쉼표 예약 판정 결과. */
enum class ArmDecision {
    /** 레버 ON + D + 이동 중 — 문장 끝에서 멈출 준비(R1). */
    ARMED,

    /** 레버 ON이지만 서 있음 — 신호 대기에서 깜빡이만 켠 경우(R3). */
    SKIP_STOPPED,

    /** 레버 ON이지만 D가 아님(P·R·N·기어 확인 안 됨). */
    SKIP_NOT_DRIVE,

    LEVER_OFF,
}
