package app.blinkercomma.engine.support

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.output.Transition
import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.reader.reduce
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 리듀서 테스트 기본 장면 — D·40km/h로 달리며 5문장 팩의 2번째 문장(index 1)을 읽는 중, 레버 내림.
 * 테스트는 이 장면에서 한 칸만 바꿔 쓴다.
 */
object ReaderScene {
    val params = CommaCardParams.DEFAULT
    val reading =
        ReaderState(clipCount = 5, phase = ReaderPhase.Reading(), index = 1, gear = Gear.DRIVE, speedMps = 40 / 3.6)

    /** 레버를 올리는 순간. */
    val armAt = 1.seconds

    /** 레버를 올린 채 문장이 끝나는 순간(예약 뒤 1초 — 상한 안). */
    val endAt = 2.seconds

    fun lever(at: Duration, signal: TurnSignal = TurnSignal.RIGHT) = ReaderEvent.Car(
        CarEvent.TurnSignalChanged(at, signal),
    )

    fun leverOff(at: Duration) = lever(at, TurnSignal.OFF)

    fun speed(at: Duration, speedMps: Double) = ReaderEvent.Car(CarEvent.SpeedChanged(at, speedMps))

    fun gear(at: Duration, gear: Gear) = ReaderEvent.Car(CarEvent.GearChanged(at, gear))

    fun clipEnded(at: Duration, index: Int = 1) = ReaderEvent.ClipEnded(at, index)

    fun tick(at: Duration) = ReaderEvent.Tick(at)

    fun press(at: Duration, source: PressSource = PressSource.MEDIA_KEY) = ReaderEvent.PlayPressed(at, source)

    fun focus(at: Duration, focus: AudioFocus) = ReaderEvent.FocusChanged(at, focus)

    /** 기본 장면에서 레버를 올린 채 문장 끝에 닿아 쉼표 중(⏸)인 상태 — 다음 문장 index 2, 레버는 아직 ON. */
    val holding: ReaderState get() = run(listOf(lever(armAt), clipEnded(endAt))).state

    fun run(
        events: List<ReaderEvent>,
        initial: ReaderState = reading,
        params: CommaCardParams = this.params,
    ): Transition<ReaderState> = replay(initial, events) { state, event -> reduce(state, event, params) }
}
