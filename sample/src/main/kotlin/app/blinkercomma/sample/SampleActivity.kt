package app.blinkercomma.sample

import ai.pleos.playground.vehicle.Vehicle
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** 화면 하나 — 상태 글자와 ▶. 50ms마다 큐 끝·엔진 시각을 확인한다. */
class SampleActivity : Activity() {
    private val main = Handler(Looper.getMainLooper())
    private val vehicle by lazy { Vehicle(this).apply { initialize() } }
    private lateinit var player: CuePlayer
    private lateinit var driver: CommaDriver
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cues = readVttCues(assets.open(VTT).bufferedReader().use { it.readText() })
        player = CuePlayer(assets, AUDIO, cues, SystemClock::elapsedRealtime)
        driver = CommaDriver(cues.size, player, ::render)
        setContentView(layout())
        SampleSignals(vehicle, ::now) { event -> main.post { driver.feed(ReaderEvent.Car(event)) } }.start()
        main.post(::poll)
    }

    override fun onDestroy() {
        main.removeCallbacksAndMessages(null)
        player.release()
        vehicle.release()
        super.onDestroy()
    }

    private fun poll() {
        player.endedCue()?.let { driver.feed(ReaderEvent.ClipEnded(now(), it)) }
        driver.tick(now())
        main.postDelayed(::poll, POLL_MS)
    }

    private fun render() {
        val phase = driver.state.phase
        val text = when {
            driver.isPauseShown -> R.string.sample_holding
            phase is ReaderPhase.Reading -> R.string.sample_reading
            else -> R.string.sample_stopped
        }
        status.text = getString(text) + "\n" + getString(R.string.sample_cue, player.index + 1, player.cues.size)
    }

    private fun layout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Style.BACKGROUND)
        addView(label(getString(R.string.sample_title), Style.TITLE_SP, Color.WHITE))
        addView(label(getString(R.string.sample_detail), Style.DETAIL_SP, Color.LTGRAY))
        status = label(getString(R.string.sample_stopped), Style.STATUS_SP, Style.AMBER).also(::addView)
        addView(
            Button(context).apply {
                text = getString(R.string.sample_play)
                textSize = Style.BUTTON_SP
                setOnClickListener { driver.feed(ReaderEvent.PlayPressed(now(), PressSource.SCREEN)) }
            },
        )
    }

    private fun label(text: String, size: Float, color: Int) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        setPadding(0, Style.GAP_PX, 0, Style.GAP_PX)
    }

    private fun now(): Duration = SystemClock.elapsedRealtime().milliseconds

    private companion object {
        const val AUDIO = "episode.m4a"
        const val VTT = "episode.vtt"
        const val POLL_MS = 50L
    }
}

/** 화면 숫자 — 글자 크기(sp)·색·간격. */
private object Style {
    val BACKGROUND = Color.rgb(16, 17, 20)
    val AMBER = Color.rgb(240, 165, 60)
    const val TITLE_SP = 40f
    const val DETAIL_SP = 26f
    const val STATUS_SP = 44f
    const val BUTTON_SP = 30f
    const val GAP_PX = 24
}
