package app.blinkercomma.engine.signal

import kotlin.time.Duration

/**
 * 차 신호 입력. 앱의 Pleos·AAOS 어댑터가 SDK 값을 이 타입으로 바꿔 넣는다(엔진은 SDK를 모른다).
 * `at`은 단조 시각(부팅 후 경과) — 엔진은 시계를 직접 읽지 않고 이 값만 쓴다.
 */
sealed interface CarEvent {
    val at: Duration

    data class TurnSignalChanged(override val at: Duration, val signal: TurnSignal) : CarEvent

    data class GearChanged(override val at: Duration, val gear: Gear) : CarEvent

    data class SpeedChanged(override val at: Duration, val speedMps: Double) : CarEvent
}
