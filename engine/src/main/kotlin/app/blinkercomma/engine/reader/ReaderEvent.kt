package app.blinkercomma.engine.reader

import app.blinkercomma.engine.signal.CarEvent
import kotlin.time.Duration

/** 엔진 입력 전부 — 차 신호·플레이어·운전자 누름·오디오 포커스·시간 흐름. */
sealed interface ReaderEvent {
    val at: Duration

    data class Car(val event: CarEvent) : ReaderEvent {
        override val at: Duration get() = event.at
    }

    /** 플레이어가 클립(=문장 하나)을 끝까지 재생했다. `index`가 지금 문장과 다르면 늦게 온 콜백이라 버린다. */
    data class ClipEnded(override val at: Duration, val index: Int) : ReaderEvent

    data class PlayPressed(override val at: Duration, val source: PressSource) : ReaderEvent

    data class PausePressed(override val at: Duration, val source: PressSource) : ReaderEvent

    /** `step` +1 = 다음 문장, -1 = 이전 문장. */
    data class SkipPressed(override val at: Duration, val step: Int) : ReaderEvent

    data class FocusChanged(override val at: Duration, val focus: AudioFocus) : ReaderEvent

    /** 시간이 흘렀음 — 앱이 [ReaderState.nextDeadline] 시각에 맞춰 보낸다(디바운스·상한 판정). */
    data class Tick(override val at: Duration) : ReaderEvent
}
