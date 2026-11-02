package app.blinkercomma.engine.pack

/** 팩 검사에서 찾는 문제. 하나라도 있으면 그 팩은 싣지 않는다. */
enum class PackProblem {
    /** 시스템 미디어 카드 제목이 6자 초과(원칙 6). */
    TITLE_TOO_LONG,

    /** 출처 크레딧 없음 — 퍼블릭 도메인 낭독이라도 출처는 밝힌다. */
    BLANK_CREDIT,
    NO_CLIPS,

    /** 길이 0 이하 클립 — 문장 끝 시각을 못 잰다. */
    ZERO_DURATION,
    DUPLICATE_FILE,
}
