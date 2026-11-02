package app.blinkercomma.engine.pack

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.serialization.Serializable

/** 콘텐츠 팩(APK 에셋 `pack.json`). 문장 1개 = 클립 1개라 문장 경계 = 클립 끝. */
@Serializable
data class SentencePack(
    /** 시스템 미디어 카드 제목 — 6자 이하(설계서 원칙 6). */
    val title: String,
    /** 낭독 출처·라이선스 한 줄 — P 화면에 그대로. */
    val credit: String,
    val clips: List<SentenceClip>,
)

@Serializable
data class SentenceClip(
    /** 팩 폴더 안 파일 이름(`s01.ogg`). */
    val file: String,
    /** 문장 본문 — 화면·시스템 미디어 카드에 띄우지 않는다(원칙 6). 자르기 검수용. */
    val text: String,
    val durationMs: Long,
) {
    val duration: Duration get() = durationMs.milliseconds
}
