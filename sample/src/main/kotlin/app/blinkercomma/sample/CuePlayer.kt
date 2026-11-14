package app.blinkercomma.sample

import android.content.res.AssetManager
import android.media.MediaPlayer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** 자막 한 줄(큐) = 엔진의 문장 하나. 시각은 음원 안 밀리초. */
data class Cue(val startMs: Int, val endMs: Int)

/**
 * 음원 하나를 이어서 틀고, 지금 큐의 끝을 지나면 멈춘 뒤 그 큐 번호를 돌려준다(엔진의 ClipEnded).
 * 다음 큐를 틀지 말지는 엔진이 정한다 — 계속이면 PlayClip(다음), 쉼표면 아무것도 안 온다.
 * 재생 위치는 「시작 위치 + 경과 시간」으로 센다 — 본 앱과 같은 이유(무음 에뮬레이터에선 플레이어 위치가 안 움직인다).
 */
class CuePlayer(assets: AssetManager, audio: String, val cues: List<Cue>, private val nowMs: () -> Long) {
    private val media = MediaPlayer().apply {
        assets.openFd(audio).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
        prepare()
    }
    private var current = 0
    private var fromMs = 0
    private var startedAtMs = 0L
    var isPlaying = false
        private set
    val index: Int get() = current

    fun play(index: Int, isFromStart: Boolean) {
        if (isFromStart || index != current) fromMs = cues[index].startMs
        current = index
        media.seekTo(fromMs)
        media.start()
        startedAtMs = nowMs()
        isPlaying = true
    }

    fun pause() {
        if (!isPlaying) return
        fromMs = positionMs()
        media.pause()
        isPlaying = false
    }

    /** 큐 끝을 지났으면 멈추고 그 번호, 아니면 null. 50ms마다 부른다. */
    fun endedCue(): Int? {
        if (!isPlaying || positionMs() < cues[current].endMs) return null
        pause()
        return current
    }

    private fun positionMs(): Int = fromMs + (nowMs() - startedAtMs).toInt()

    fun release() = media.release()
}

/** WebVTT 시각 줄(「00:00:09.000 --> 00:00:12.500」)만 읽는다 — 글자는 엔진에 필요 없다. */
fun readVttCues(text: String): List<Cue> = text.lineSequence()
    .mapNotNull { CUE_TIME.find(it) }
    .map { Cue(it.groupValues[1].toMillis(), it.groupValues[2].toMillis()) }
    .toList()

private val CUE_TIME = Regex("""(\d{2}:\d{2}:\d{2}\.\d{3})\s*-->\s*(\d{2}:\d{2}:\d{2}\.\d{3})""")

private fun String.toMillis(): Int {
    val (h, m, s) = split(":")
    return (h.toInt().hours + m.toInt().minutes + s.toDouble().seconds).inWholeMilliseconds.toInt()
}
