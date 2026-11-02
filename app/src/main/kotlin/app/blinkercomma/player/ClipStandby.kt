package app.blinkercomma.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.Executors

/**
 * 다음에 쓸 클립 몇 개를 미리 준비해 두는 곳. 준비(prepare)는 자기 스레드에서, 목록은 [audio] 스레드에서만 만진다.
 * 까닭(D4 리허설 실측 — docs/progress.md D4): 에뮬레이터에서 준비가 가끔 5.9~7.6초 막혔고, 같은 스레드에서 기다리던
 * 다음 문장 시작이 그만큼(최대 3.0초) 늦었다. 준비된 플레이어의 시작은 그동안에도 막히지 않았다 — 그래서 준비를
 * 소리 내는 스레드에서 떼고 두 문장 앞까지 준비해 둔다(한 번 막혀도 바로 다음 문장은 이미 준비돼 있게).
 */
internal class ClipStandby(
    private val audio: Handler,
    private val prepare: (String) -> MediaPlayer,
    private val retire: (MediaPlayer) -> Unit,
) {
    private val preparer = Executors.newSingleThreadExecutor()
    private val ready = mutableMapOf<String, MediaPlayer>()
    private val inFlight = mutableSetOf<String>()
    private var wanted: List<String> = emptyList()

    /** 바라는 목록으로 바꾼다 — 밖의 준비된 것은 놓고, 없는 것은 준비를 건다. 건 준비가 다 끝나면 [afterAll]. */
    fun want(paths: List<String>, afterAll: () -> Unit) {
        wanted = paths
        ready.keys.filter { it !in paths }.forEach { path -> ready.remove(path)?.let(retire) }
        paths.filter { it !in ready && it !in inFlight }.forEach(::prepareLater)
        preparer.execute { audio.post(afterAll) }
    }

    /** 준비된 것이 있으면 넘겨준다(목록에서 빠짐). 없으면 null — 부르는 쪽이 그 자리에서 준비한다. */
    fun take(path: String): MediaPlayer? = ready.remove(path)

    fun shutdown() {
        wanted = emptyList()
        ready.values.forEach(retire)
        ready.clear()
        preparer.shutdown()
    }

    private fun prepareLater(path: String) {
        inFlight += path
        preparer.execute {
            val begin = SystemClock.elapsedRealtime()
            val player = prepare(path)
            Log.i(TAG, "preload prepareMs=${SystemClock.elapsedRealtime() - begin}")
            audio.post { settle(path, player) }
        }
    }

    private fun settle(path: String, player: MediaPlayer) {
        inFlight -= path
        if (path in wanted && path !in ready) ready[path] = player else retire(player)
    }

    private companion object {
        const val TAG = "CommaDeck"
    }
}
