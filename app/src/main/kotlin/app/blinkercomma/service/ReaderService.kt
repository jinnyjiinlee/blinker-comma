package app.blinkercomma.service

import ai.pleos.playground.vehicle.Vehicle
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.media.MediaBrowserServiceCompat
import app.blinkercomma.BlinkerApp
import app.blinkercomma.data.LoadedPack
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.monotonicNow
import app.blinkercomma.player.ClipPlayer
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

/**
 * 낭독 서비스 하나 — MediaBrowserService(Pleos 미디어 앱 필수) + MediaSession + 차 신호 → 엔진 → 효과.
 * 재생이 시작될 때만 포그라운드로 올리고, 정지(Stopped)면 내린다. 연결·시작만으로는 재생하지 않는다(원칙 5).
 * 생명주기 스코프를 쓰려고 `LifecycleService` 대신 디스패처를 직접 단다(MBS가 이미 부모 클래스라서).
 */
class ReaderService : MediaBrowserServiceCompat(), LifecycleOwner {
    private val dispatcher = ServiceLifecycleDispatcher(this)
    override val lifecycle: Lifecycle get() = dispatcher.lifecycle
    private val container by lazy { (application as BlinkerApp).container }
    private val vehicle by lazy { Vehicle(this).apply { initialize() } }
    private lateinit var media: ReaderMediaSession
    private lateinit var player: ClipPlayer
    private lateinit var focus: AudioFocusGate
    private var isForeground = false

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        val session = container.session
        media = ReaderMediaSession(this, packTitle(), session::onEvent, ::monotonicNow)
        sessionToken = media.session.sessionToken
        player = ClipPlayer(this, container.pack, session::onEvent, session::onSoundStarted, ::monotonicNow)
        focus = AudioFocusGate(this, session::onEvent, ::monotonicNow)
        session.attach(EffectRunner(player, focus, container.tallyStore, ::startForegroundOnce)::run)
        lifecycleScope.launch { session.state.distinctUntilChangedBy { it.phase }.collect { onPhase(it.phase) } }
        lifecycleScope.launch { carEvents(this@ReaderService, vehicle).collect(session::onEvent) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        player.release()
        focus.release()
        media.release()
        vehicle.release()
        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot =
        BrowserRoot(ROOT_ID, null)

    // 팩 하나뿐이라 목록은 비운다 — 재생은 ▶·핸들 키로만(목록에서 골라 바로 재생되는 길을 만들지 않음)
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) =
        result.sendResult(mutableListOf())

    private fun onPhase(phase: ReaderPhase) {
        media.show(phase)
        if (phase != ReaderPhase.Stopped) return
        focus.release()
        if (isForeground) ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    private fun startForegroundOnce() {
        if (isForeground) return
        startService(Intent(this, ReaderService::class.java))
        val type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildReaderNotification(this), type)
        isForeground = true
    }

    private fun packTitle(): String = (container.pack as? LoadedPack.Ready)?.pack?.title.orEmpty()

    private companion object {
        const val NOTIFICATION_ID = 1
        const val ROOT_ID = "root"
    }
}
