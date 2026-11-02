package app.blinkercomma.player

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** 열어 달라는 한 건 — `ticket`이 지금 바라는 표와 다르면(그사이 멈춤·다른 재생) 준비만 하고 소리는 내지 않는다. */
internal data class ClipRequest(val ticket: Int, val path: String, val fromMs: Long)

/**
 * MediaPlayer를 실제로 만지는 곳 — 전용 스레드 하나에서만(넘겨받은 [audio] 핸들러). 소리 내는 플레이어 하나 +
 * 다음에 쓸 클립을 미리 준비해 둔 대기 플레이어 하나. 까닭(D2 실측 — docs/signals_measured.md): 에뮬레이터에서
 * 준비(prepare)가 가끔 2.3~3.8초 막혀 재개가 늦었다 — 미리 준비해 두면 재개는 시작(start)만. 놓기(release)는 또
 * 다른 스레드에서(최대 7.6초). 준비 중 멈춤이 오면 늦게 끝난 준비가 소리를 내지 않게 시작 직전에 표를 다시 본다.
 */
internal class ClipDeck(private val context: Context, private val audio: Handler) {
    private val releaser = Executors.newSingleThreadExecutor()
    private val wanted = AtomicInteger(NONE)
    private var current: MediaPlayer? = null
    private var standby: Pair<String, MediaPlayer>? = null

    /** 메인 스레드에서 부른다 — 바라는 표는 바로 바꾸고, 실제 열기는 전용 스레드에서. */
    fun open(request: ClipRequest, onStarted: (Duration) -> Unit, onCompleted: () -> Unit) {
        wanted.set(request.ticket)
        audio.post { openNow(request, onStarted, onCompleted) }
    }

    fun close() {
        wanted.set(NONE)
        audio.post { closeNow() }
    }

    /** 다음에 쓸 것 같은 클립을 미리 준비한다(다음 문장, 또는 멈춘 그 문장). 이미 준비돼 있으면 그대로. */
    fun preload(path: String) = audio.post {
        if (standby?.first == path) return@post
        standby?.second?.let(::retire)
        standby = path to prepared(path)
    }

    /** 앱이 끝날 때 — 플레이어를 다 놓고 놓기 스레드도 닫는다. */
    fun shutdown() {
        close()
        audio.post {
            standby?.second?.let(::retire)
            standby = null
            releaser.shutdown()
        }
    }

    private fun openNow(request: ClipRequest, onStarted: (Duration) -> Unit, onCompleted: () -> Unit) {
        closeNow()
        val begin = SystemClock.elapsedRealtime()
        val isWarm = standby?.first == request.path
        val player = takeStandby(request.path) ?: prepared(request.path)
        val ready = SystemClock.elapsedRealtime()
        if (wanted.get() != request.ticket) return dropLate(player, ready - begin)
        if (request.fromMs > 0) player.seekTo(request.fromMs.toInt())
        player.setOnCompletionListener { done -> if (done === current) onCompleted() }
        player.start()
        current = player
        val started = SystemClock.elapsedRealtime()
        Log.i(TAG, "open warm=$isWarm prepareMs=${ready - begin} startMs=${started - ready}")
        onStarted(started.milliseconds)
    }

    private fun takeStandby(path: String): MediaPlayer? =
        standby?.takeIf { it.first == path }?.second?.also { standby = null }

    private fun prepared(path: String): MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(SPEECH)
        context.assets.openFd(path).use { file -> setDataSource(file.fileDescriptor, file.startOffset, file.length) }
        setOnErrorListener { _, what, extra -> Log.e(TAG, "player error what=$what extra=$extra").let { false } }
        prepare()
    }

    private fun dropLate(player: MediaPlayer, prepareMs: Long) {
        Log.i(TAG, "open dropped — cancelled while preparing (prepareMs=$prepareMs)")
        retire(player)
    }

    private fun closeNow() {
        current?.let(::retire)
        current = null
    }

    // 옛 플레이어엔 release 말고 아무것도 부르지 않는다 — setVolume 한 번이 8.5초 막혔다(에뮬레이터 실측)
    private fun retire(player: MediaPlayer) = releaser.execute {
        val begin = SystemClock.elapsedRealtime()
        player.release()
        Log.i(TAG, "release ms=${SystemClock.elapsedRealtime() - begin}")
    }

    private companion object {
        const val TAG = "CommaDeck"
        const val NONE = 0
    }
}
