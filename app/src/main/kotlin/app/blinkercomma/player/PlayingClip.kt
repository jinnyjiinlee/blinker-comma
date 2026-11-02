package app.blinkercomma.player

import android.media.AudioAttributes
import app.blinkercomma.engine.pack.SentenceClip
import kotlin.time.Duration

/** 재생 중인 문장 — 위치는 플레이어가 아니라 단조 시계로 잰다(에뮬레이터 플레이어 위치는 실제 시간보다 느렸음). */
internal data class PlayingClip(val index: Int, val fromMs: Long, val startedAt: Duration) {
    fun positionMs(now: Duration, clip: SentenceClip?): Long {
        val playedMs = fromMs + (now - startedAt).inWholeMilliseconds
        return clip?.let { playedMs.coerceAtMost(it.durationMs) } ?: playedMs
    }
}

/** 포커스로 잠시 멈춘 문장과 자리 — 돌려받으면 여기부터(R10). */
internal data class PausedClip(val index: Int, val positionMs: Long)

internal val SPEECH: AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()
