package app.blinkercomma.engine.pack

import kotlin.test.Test
import kotlin.test.assertEquals

/** 설계서 「콘텐츠 팩」 검사 — 제목 6자 초과(원칙 6)·크레딧 없음·클립 0개·길이 0·파일 중복. */
class ClassifyPackProblemsTest {
    private val clip = SentenceClip(file = "s01.ogg", text = "One.", durationMs = 2600)
    private val validPack = SentencePack(
        title = "낭독 1과",
        credit = "Public domain reading",
        clips = listOf(clip, clip.copy(file = "s02.ogg")),
    )

    @Test
    fun `정상 팩은 문제가 없다`() {
        assertEquals(emptyList(), classifyPackProblems(validPack))
    }

    @Test
    fun `제목이 상한보다 한 자 짧으면 문제가 없다`() {
        assertEquals(emptyList(), classifyPackProblems(validPack.copy(title = "가".repeat(MAX_TITLE_CHARS - 1))))
    }

    @Test
    fun `제목이 상한과 같으면 문제가 없다`() {
        assertEquals(emptyList(), classifyPackProblems(validPack.copy(title = "가".repeat(MAX_TITLE_CHARS))))
    }

    @Test
    fun `제목이 상한보다 한 자 길면 제목 초과다`() {
        val pack = validPack.copy(title = "가".repeat(MAX_TITLE_CHARS + 1))
        assertEquals(listOf(PackProblem.TITLE_TOO_LONG), classifyPackProblems(pack))
    }

    @Test
    fun `크레딧이 비어 있으면 크레딧 없음이다`() {
        assertEquals(listOf(PackProblem.BLANK_CREDIT), classifyPackProblems(validPack.copy(credit = " ")))
    }

    @Test
    fun `클립이 없으면 클립 없음이다`() {
        assertEquals(listOf(PackProblem.NO_CLIPS), classifyPackProblems(validPack.copy(clips = emptyList())))
    }

    @Test
    fun `길이 0인 클립이 있으면 길이 0이다`() {
        val pack = validPack.copy(clips = listOf(clip, clip.copy(file = "s02.ogg", durationMs = 0)))
        assertEquals(listOf(PackProblem.ZERO_DURATION), classifyPackProblems(pack))
    }

    @Test
    fun `같은 파일을 두 번 쓰면 파일 중복이다`() {
        assertEquals(
            listOf(PackProblem.DUPLICATE_FILE),
            classifyPackProblems(validPack.copy(clips = listOf(clip, clip))),
        )
    }
}
