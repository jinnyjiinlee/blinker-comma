package app.blinkercomma.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.ReaderEvent
import kotlin.time.Duration

/**
 * 오디오 포커스 — 재생할 때 GAIN을 잡고, 변화는 [ReaderEvent.FocusChanged]로(R10). 쉼표는 포커스로 만들지 않는다
 * (원칙 7) — 쉼표 중에도 포커스는 쥐고 있고, 정지(Stopped)에서만 돌려준다.
 * 낭독은 줄이면 못 알아들어서 「줄여도 됨」도 잠시 멈춤으로 받는다(`setWillPauseWhenDucked`).
 */
class AudioFocusGate(context: Context, private val onEvent: (ReaderEvent) -> Unit, private val now: () -> Duration) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(::onChange)
        .build()
    private var isHeld = false

    fun hold() {
        if (isHeld) return
        val result = audio.requestAudioFocus(request)
        isHeld = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.i(TAG, "focus request=$result")
    }

    fun release() {
        if (!isHeld) return
        isHeld = false
        Log.i(TAG, "focus abandon=${audio.abandonAudioFocusRequest(request)}")
    }

    private fun onChange(change: Int) {
        Log.i(TAG, "focus change=$change")
        val focus = when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> AudioFocus.GAIN
            AudioManager.AUDIOFOCUS_LOSS -> AudioFocus.LOSS.also { isHeld = false }
            else -> AudioFocus.LOSS_TRANSIENT
        }
        onEvent(ReaderEvent.FocusChanged(now(), focus))
    }

    private companion object {
        const val TAG = "CommaFocus"
    }
}
