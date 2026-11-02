package app.blinkercomma.engine.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

/** 설계서 「콘텐츠 팩」 pack.json 형식(title·credit·clips[file·text·durationMs]). */
class PackFileTest {
    private val validJson = """
        {"title": "낭독 1과", "credit": "Public domain reading",
         "clips": [{"file": "s01.ogg", "text": "One.", "durationMs": 2600},
                   {"file": "s02.ogg", "text": "Two.", "durationMs": 3100}]}
    """.trimIndent()

    @Test
    fun `정상 팩은 문장 길이까지 읽힌다`() {
        val parsed = assertIs<PackFile.Parsed>(toPackFile(validJson))
        assertEquals(listOf(2600.milliseconds, 3100.milliseconds), parsed.pack.clips.map { it.duration })
    }

    @Test
    fun `크레딧 칸이 빠진 팩은 못 읽음으로 돌려준다`() {
        assertIs<PackFile.Unreadable>(toPackFile(validJson.replace("\"credit\": \"Public domain reading\",", "")))
    }

    @Test
    fun `모르는 칸이 있는 팩은 못 읽음으로 돌려준다`() {
        assertIs<PackFile.Unreadable>(toPackFile(validJson.replace("\"title\"", "\"speed\": 1, \"title\"")))
    }

    @Test
    fun `JSON이 아니면 못 읽음으로 돌려준다`() {
        assertIs<PackFile.Unreadable>(toPackFile("not json"))
    }
}
