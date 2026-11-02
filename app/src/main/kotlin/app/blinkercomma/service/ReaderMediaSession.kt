package app.blinkercomma.service

import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import kotlin.time.Duration

/**
 * MediaSession — 핸들 미디어 키·시스템 미디어 카드가 들어오는 길. 메타는 제목(≤6자) 하나뿐, 문장 본문 0(원칙 6).
 * PLAY_PAUSE 키는 세션 기본 처리가 「재생 중이면 onPause, 아니면 onPlay」로 바꿔 준다 — 쉼표 중은 PAUSED로 알려
 * 키 한 번이 즉시 재개(R9)가 되게 한다.
 */
class ReaderMediaSession(
    context: Context,
    title: String,
    private val onEvent: (ReaderEvent) -> Unit,
    now: () -> Duration,
) {
    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() = onEvent(ReaderEvent.PlayPressed(now(), PressSource.MEDIA_KEY))
        override fun onPause() = onEvent(ReaderEvent.PausePressed(now(), PressSource.MEDIA_KEY))
        override fun onSkipToNext() = onEvent(ReaderEvent.SkipPressed(now(), step = 1))
        override fun onSkipToPrevious() = onEvent(ReaderEvent.SkipPressed(now(), step = -1))
        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val key = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            Log.i(TAG, "media button code=${key?.keyCode} action=${key?.action}")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }
    }
    val session = MediaSessionCompat(context, TAG).apply {
        setCallback(callback)
        setMetadata(MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_TITLE, title).build())
        setPlaybackState(toPlaybackState(ReaderPhase.Stopped))
        isActive = true
    }

    fun show(phase: ReaderPhase) = session.setPlaybackState(toPlaybackState(phase))

    fun release() = session.release()

    private companion object {
        const val TAG = "CommaMedia"
    }
}

private const val ACTIONS = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
    PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

// 정지도 PAUSED로 — STOPPED면 키 라우팅에서 빠질 수 있다. 위치는 알리지 않는다(문장 단위라 진행 막대가 뜻이 없음)
private fun toPlaybackState(phase: ReaderPhase): PlaybackStateCompat {
    val isPlaying = phase is ReaderPhase.Reading
    val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
    return PlaybackStateCompat.Builder()
        .setActions(ACTIONS)
        .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
        .build()
}
