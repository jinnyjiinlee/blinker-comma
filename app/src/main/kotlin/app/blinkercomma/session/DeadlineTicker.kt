package app.blinkercomma.session

import android.os.Handler
import android.os.Looper
import kotlin.time.Duration

/**
 * 엔진 마감 시각(디바운스·상한)에 한 번 깨운다. 100ms 폴링이면 로그 `OFF held`가 0.80~0.89초로 흔들려
 * 대본 글자와 안 맞는다(설계서 D1 변경). 새 마감이 오면 옛 것은 지운다.
 */
class DeadlineTicker(private val now: () -> Duration) {
    private val main = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null

    fun schedule(deadline: Duration?, onDue: () -> Unit) {
        pending?.let(main::removeCallbacks)
        pending = null
        if (deadline == null) return
        val due = Runnable {
            pending = null
            onDue()
        }
        pending = due
        main.postDelayed(due, (deadline - now()).inWholeMilliseconds.coerceAtLeast(0))
    }
}
