package app.blinkercomma.engine.pack

import kotlinx.serialization.json.Json

/** 팩 JSON 읽기 결과 — 깨진 파일은 예상되는 실패라 예외 대신 결과로. */
sealed interface PackFile {
    data class Parsed(val pack: SentencePack) : PackFile

    data class Unreadable(val detail: String) : PackFile
}

private val packJson = Json { ignoreUnknownKeys = false }

fun toPackFile(text: String): PackFile = try {
    PackFile.Parsed(packJson.decodeFromString(SentencePack.serializer(), text))
} catch (error: IllegalArgumentException) {
    // kotlinx SerializationException도 IllegalArgumentException — 형식 오류·빠진 칸 모두 여기로
    PackFile.Unreadable(error.message.orEmpty())
}
