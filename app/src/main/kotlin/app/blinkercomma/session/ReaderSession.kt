package app.blinkercomma.session

import android.util.Log
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.reader.reduce
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 엔진 상태 한 곳. 모든 이벤트는 메인 스레드에서 들어온다(콜백 순서 = 처리 순서).
 * 효과는 나온 순서대로 바로 실행하고, 다음 [ReaderEvent.Tick]은 `nextDeadline` 시각에 한 번만 건다.
 * ⏸는 소리가 멎는 순간 켜고 다시 나는 순간 끈다(설계서) — 재개 때는 플레이어의 [onSoundStarted]를 기다린다.
 */
class ReaderSession(
    initial: ReaderState,
    private val params: () -> CommaCardParams,
    private val now: () -> Duration,
) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<ReaderState> = _state.asStateFlow()
    private val _isHoldGlyphOn = MutableStateFlow(false)

    /** D 화면 ⏸ — 상태가 아니라 효과로 켜고 끈다(소리가 멎는 순간 켜고 다시 날 때 끔). */
    val isHoldGlyphOn: StateFlow<Boolean> = _isHoldGlyphOn.asStateFlow()
    private val ticker = DeadlineTicker(now)
    private var runEffect: (Effect) -> Unit = {}

    /** 재개 때 ⏸ 끄기를 소리가 실제로 날 때까지 미룸 — 플레이어 열기가 늦을 수 있다(에뮬레이터 실측 최대 4.7초). */
    private var isGlyphOffPending = false

    /** 효과 실행자(서비스) 연결. 서비스가 뜨기 전 효과는 버려진다 — 재생은 서비스가 뜬 뒤에만 누를 수 있다. */
    fun attach(runner: (Effect) -> Unit) {
        runEffect = runner
    }

    fun onEvent(event: ReaderEvent) {
        val transition = reduce(_state.value, event, params())
        _state.value = transition.state
        val isPlaying = transition.effects.any { it is Effect.PlayClip }
        transition.effects.forEach { effect ->
            if (effect is Effect.ShowHoldGlyph) showGlyph(effect.isOn, isPlaying) else runEffect(effect)
        }
        ticker.schedule(transition.state.nextDeadline(params())) { onEvent(ReaderEvent.Tick(now())) }
    }

    /** 플레이어가 소리를 실제로 냈다 — 미뤄 둔 ⏸ 끄기를 여기서. */
    fun onSoundStarted() {
        if (!isGlyphOffPending) return
        isGlyphOffPending = false
        setGlyph(false)
    }

    private fun showGlyph(isOn: Boolean, isWaitingForSound: Boolean) {
        isGlyphOffPending = !isOn && isWaitingForSound
        if (!isGlyphOffPending) setGlyph(isOn)
    }

    private fun setGlyph(isOn: Boolean) {
        _isHoldGlyphOn.value = isOn
        Log.i(GLYPH_TAG, "holdGlyph=${if (isOn) "on" else "off"}")
    }

    private companion object {
        const val GLYPH_TAG = "CommaGlyph"
    }
}
