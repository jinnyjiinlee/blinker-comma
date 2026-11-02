package app.blinkercomma.engine.pack

/** 시스템 미디어 카드 제목 상한(설계서 원칙 6 — 예: 「낭독 1과」). */
const val MAX_TITLE_CHARS = 6

/** 팩이 앱에 실을 만한가 — 문제 목록(빈 목록 = 정상). 글자 수는 코드 포인트로 센다. */
fun classifyPackProblems(pack: SentencePack): List<PackProblem> {
    val files = pack.clips.map { it.file }
    return listOfNotNull(
        PackProblem.TITLE_TOO_LONG.takeIf { pack.title.codePointCount(0, pack.title.length) > MAX_TITLE_CHARS },
        PackProblem.BLANK_CREDIT.takeIf { pack.credit.isBlank() },
        PackProblem.NO_CLIPS.takeIf { pack.clips.isEmpty() },
        PackProblem.ZERO_DURATION.takeIf { pack.clips.any { clip -> clip.durationMs <= 0 } },
        PackProblem.DUPLICATE_FILE.takeIf { files.size != files.toSet().size },
    )
}
