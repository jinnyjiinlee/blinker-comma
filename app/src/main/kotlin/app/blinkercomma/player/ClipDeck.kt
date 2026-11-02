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
 * 소리 내는 플레이어를 만지는 곳 — 열기·시작·멈춤은 전용 스레드 하나에서만(넘겨받은 [audio] 핸들러). 까닭(D2·D4
 * 실측 — docs/signals_measured.md·progress.md): 에뮬레이터에서 준비(prepare)·놓기(release)가 가끔 수 초 막힌다 →
 * 미리 준비는 [ClipStandby](자기 스레드, 두 문장 앞까지), 놓기는 또 다른 스레드, 시계로 끝난 문장의 것은 다음 준비
 * 뒤에([PlayerHandoff]). 그러면 재개·다음 문장은 시작(start)만. 준비 중 멈춤이 오면 늦게 끝난 준비가 소리를 내지
 * 않게 시작 직전에 표를 다시 본다.
 */
internal class ClipDeck(private val context: Context, private val audio: Handler) {
    private val releaser = Executors.newSingleThreadExecutor()
    private val wanted = AtomicInteger(NONE)
    private val handoff = PlayerHandoff<MediaPlayer>()
    private val standby = ClipStandby(audio, ::prepared, ::retire)

    /** 메인 스레드에서 부른다 — 바라는 표는 바로 바꾸고, 실제 열기는 전용 스레드에서. */
    fun open(request: ClipRequest, onStarted: (Duration) -> Unit, onCompleted: () -> Unit) {
        wanted.set(request.ticket)
        audio.post { openNow(request, onStarted, onCompleted) }
    }

    fun close() {
        wanted.set(NONE)
        audio.post { closeNow() }
    }

    /** 시계로 문장이 끝남 — 소리는 이미 끝났으니 놓기는 다음 준비 뒤로 미룬다. */
    fun finish() {
        wanted.set(NONE)
        audio.post { handoff.finish().forEach(::retire) }
    }

    /** 다음에 쓸 것 같은 클립들을 미리 준비한다(앞 문장부터). 준비가 끝나면 끝난 문장 플레이어를 놓는다. */
    fun preload(paths: List<String>) = audio.post {
        standby.want(paths) { handoff.afterPreload().forEach(::retire) }
    }

    /** 앱이 끝날 때 — 플레이어를 다 놓고 놓기 스레드도 닫는다. */
    fun shutdown() {
        close()
        audio.post {
            standby.shutdown()
            releaser.shutdown()
        }
    }

    private fun openNow(request: ClipRequest, onStarted: (Duration) -> Unit, onCompleted: () -> Unit) {
        val begin = SystemClock.elapsedRealtime()
        val warm = standby.take(request.path)
        val player = warm ?: prepared(request.path)
        val ready = SystemClock.elapsedRealtime()
        if (wanted.get() != request.ticket) return dropLate(player, ready - begin)
        if (request.fromMs > 0) player.seekTo(request.fromMs.toInt())
        player.setOnCompletionListener { done -> if (done === handoff.current) onCompleted() }
        player.start()
        handoff.start(player).forEach(::retire)
        val started = SystemClock.elapsedRealtime()
        Log.i(TAG, "open warm=${warm != null} prepareMs=${ready - begin} startMs=${started - ready}")
        onStarted(started.milliseconds)
    }

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

    private fun closeNow() = handoff.stop().forEach(::retire)

    // 옛 플레이어엔 release 말고 아무것도 부르지 않는다 — setVolume 한 번이 8.5초 막혔다(에뮬레이터 실측)
    // 앱이 끝난 뒤 늦게 끝난 준비는 그 자리에서 놓는다(놓기 스레드는 이미 닫힘)
    private fun retire(player: MediaPlayer) {
        if (releaser.isShutdown) return player.release()
        releaser.execute {
            val begin = SystemClock.elapsedRealtime()
            player.release()
            Log.i(TAG, "release ms=${SystemClock.elapsedRealtime() - begin}")
        }
    }

    private companion object {
        const val TAG = "CommaDeck"
        const val NONE = 0
    }
}
