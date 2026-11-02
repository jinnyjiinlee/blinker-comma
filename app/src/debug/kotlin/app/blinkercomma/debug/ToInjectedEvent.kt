package app.blinkercomma.debug

import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.time.Duration

/**
 * 주입 한 줄(`signal`·`value`) → 엔진 이벤트. 모르는 이름·값이면 null(무시하고 로그만).
 * 속도는 엔진과 같은 m/s(`speed_mps`). 핸들 키는 MediaSession 기본 처리 뒤의 모양(PLAY·PAUSE·NEXT·PREVIOUS)으로.
 */
fun toInjectedEvent(signal: String, value: String, at: Duration): ReaderEvent? = when (signal) {
    "turn_signal", "gear", "speed_mps" -> toCarEvent(signal, value, at)?.let { ReaderEvent.Car(it) }
    "focus" -> enumOrNull<AudioFocus>(value)?.let { ReaderEvent.FocusChanged(at, it) }
    "media_key" -> toMediaKeyEvent(value, at)
    else -> null
}

private fun toCarEvent(signal: String, value: String, at: Duration): CarEvent? = when (signal) {
    "turn_signal" -> enumOrNull<TurnSignal>(value)?.let { CarEvent.TurnSignalChanged(at, it) }
    "gear" -> enumOrNull<Gear>(value)?.let { CarEvent.GearChanged(at, it) }
    else -> value.toDoubleOrNull()?.let { CarEvent.SpeedChanged(at, it) }
}

private fun toMediaKeyEvent(value: String, at: Duration): ReaderEvent? = when (value) {
    "PLAY" -> ReaderEvent.PlayPressed(at, PressSource.MEDIA_KEY)
    "PAUSE" -> ReaderEvent.PausePressed(at, PressSource.MEDIA_KEY)
    "NEXT" -> ReaderEvent.SkipPressed(at, step = 1)
    "PREVIOUS" -> ReaderEvent.SkipPressed(at, step = -1)
    else -> null
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? = enumValues<T>().firstOrNull { it.name == name }
