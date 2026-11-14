package app.blinkercomma.sample

import android.util.Log
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.reader.reduce
import kotlin.time.Duration

/**
 * 연동의 전부 — 이벤트를 엔진에 넣고, 나온 효과대로만 한다(docs/INTEGRATION.md 1절).
 * 이 샘플은 자동 재개를 켠다: 핸들 키(MediaSession) 없이 레버만으로 시연하려고.
 */
class CommaDriver(cueCount: Int, private val player: CuePlayer, private val onState: () -> Unit) {
    private val params = CommaCardParams.DEFAULT.copy(isAutoResume = true)
    var state = ReaderState(clipCount = cueCount)
        private set
    var isPauseShown = false
        private set

    fun feed(event: ReaderEvent) {
        val result = reduce(state, event, params)
        state = result.state
        result.effects.forEach(::apply)
        onState()
    }

    /** 상한(2.5초)·디바운스(0.8초) 판정 시각이 지났으면 시간 흐름을 알린다. */
    fun tick(now: Duration) {
        state.nextDeadline(params)?.takeIf { now >= it }?.let { feed(ReaderEvent.Tick(now)) }
    }

    private fun apply(effect: Effect) {
        when (effect) {
            is Effect.PlayClip -> player.play(effect.index, effect.isFromStart)
            Effect.PauseClip -> player.pause()
            is Effect.ShowHoldGlyph -> isPauseShown = effect.isOn
            is Effect.WriteLog -> Log.i(TAG, effect.line)
            is Effect.SaveTally -> Unit
        }
    }

    private companion object {
        const val TAG = "Comma"
    }
}
