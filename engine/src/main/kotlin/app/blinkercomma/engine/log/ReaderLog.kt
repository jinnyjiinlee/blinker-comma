package app.blinkercomma.engine.log

import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.time.Duration

/**
 * 로그 줄(태그 `Comma`) 종류. 형식은 설계서 「쉼표 규칙」 아래 ⓐ~ⓓ와 보조 줄 — 대본 anchor와
 * 시나리오 테스트가 이 글자를 그대로 찾으므로 바꾸면 대본·설계서도 같이 고친다.
 */
sealed interface ReaderLog {
    val line: String

    /** ⓐ·ⓒ */
    data class TurnSignalSeen(val signal: TurnSignal) : ReaderLog {
        override val line: String get() = "turnSignal=$signal"
    }

    data class GearSeen(val gear: Gear) : ReaderLog {
        override val line: String get() = "gear=$gear"
    }

    data object Armed : ReaderLog {
        override val line: String = "moving → comma armed"
    }

    data object ArmedReverse : ReaderLog {
        override val line: String = "reverse → comma armed"
    }

    data object CommaCancelled : ReaderLog {
        override val line: String = "comma cancelled"
    }

    data class NoCommaStopped(val speedMps: Double) : ReaderLog {
        override val line: String get() = "lever=ON speed=${formatDecimal(speedMps)} → no comma"
    }

    /** `gear=null` = 기어 확인 안 됨. */
    data class NoCommaNotDrive(val gear: Gear?) : ReaderLog {
        override val line: String get() = "lever=ON gear=$gear → no comma"
    }

    /** ⓑ */
    data class HeldAtEnd(val index: Int) : ReaderLog {
        override val line: String get() = "clipEnd ${sentenceLabel(index)} lever=ON → hold"
    }

    data class HeldMid(val index: Int, val waited: Duration) : ReaderLog {
        override val line: String get() = "endWait ${formatSeconds(waited)} → hold mid ${sentenceLabel(index)}"
    }

    /** ⓓ */
    data class ResumedAfterOff(val held: Duration, val index: Int) : ReaderLog {
        override val line: String get() = "OFF held ${formatSeconds(held)} → play ${sentenceLabel(index)}"
    }

    data object KeptHold : ReaderLog {
        override val line: String = "autoResume=off → keep hold"
    }

    data class PlayedByPress(val source: PressSource, val index: Int) : ReaderLog {
        override val line: String get() = "${source.logName} PLAY → play ${sentenceLabel(index)}"
    }

    data class StoppedByPress(val source: PressSource, val index: Int) : ReaderLog {
        override val line: String get() = "${source.logName} PAUSE → stop ${sentenceLabel(index)}"
    }

    data class Skipped(val step: Int, val index: Int) : ReaderLog {
        override val line: String get() = "skip ${if (step > 0) "+$step" else "$step"} → ${sentenceLabel(index)}"
    }

    data class FocusSeen(val focus: AudioFocus) : ReaderLog {
        override val line: String get() = "focus=$focus"
    }

    /** 클립 재생 명령 순간 — 대본 anchor `clipStart`가 찾는 줄. */
    data class ClipStarted(val index: Int) : ReaderLog {
        override val line: String get() = "clipStart ${sentenceLabel(index)}"
    }

    data object PackFinished : ReaderLog {
        override val line: String = "pack end → stop"
    }
}
