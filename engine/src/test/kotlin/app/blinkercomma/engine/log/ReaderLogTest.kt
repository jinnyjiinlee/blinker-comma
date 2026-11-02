package app.blinkercomma.engine.log

import app.blinkercomma.engine.reader.AudioFocus
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.signal.TurnSignal
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** 설계서 「쉼표 규칙」 아래 로그 형식 ⓐ~ⓓ·보조 줄 — 대본 anchor와 영상 PiP가 이 글자를 그대로 쓴다. */
class ReaderLogTest {
    @Test
    fun `ⓐ 레버를 올리면 오른쪽 깜빡이 줄이다`() {
        assertEquals("turnSignal=RIGHT", ReaderLog.TurnSignalSeen(TurnSignal.RIGHT).line)
    }

    @Test
    fun `ⓑ 문장 끝 멈춤 줄은 끝난 문장 번호를 1부터 쓴다`() {
        assertEquals("clipEnd s=2 lever=ON → hold", ReaderLog.HeldAtEnd(index = 1).line)
    }

    @Test
    fun `ⓒ 레버를 내리면 깜빡이 꺼짐 줄이다`() {
        assertEquals("turnSignal=OFF", ReaderLog.TurnSignalSeen(TurnSignal.OFF).line)
    }

    @Test
    fun `ⓓ 재개 줄은 유지 시간을 소수 둘째 자리로 쓴다`() {
        assertEquals("OFF held 0.80s → play s=3", ReaderLog.ResumedAfterOff(800.milliseconds, index = 2).line)
    }

    @Test
    fun `서 있어서 예약 안 한 줄은 속도 0을 정수로 쓴다`() {
        assertEquals("lever=ON speed=0 → no comma", ReaderLog.NoCommaStopped(0.0).line)
    }

    @Test
    fun `서 있어서 예약 안 한 줄은 소수 속도를 둘째 자리까지 쓴다`() {
        assertEquals("lever=ON speed=0.28 → no comma", ReaderLog.NoCommaStopped(1 / 3.6).line)
    }

    @Test
    fun `기어를 모르면 기어 칸에 null을 쓴다`() {
        assertEquals("lever=ON gear=null → no comma", ReaderLog.NoCommaNotDrive(null).line)
    }

    @Test
    fun `문장 중간 멈춤 줄은 기다린 시간을 쓴다`() {
        assertEquals("endWait 2.50s → hold mid s=1", ReaderLog.HeldMid(index = 0, waited = 2.5.seconds).line)
    }

    @Test
    fun `핸들 키 재개 줄은 mediaKey로 시작한다`() {
        assertEquals("mediaKey PLAY → play s=3", ReaderLog.PlayedByPress(PressSource.MEDIA_KEY, index = 2).line)
    }

    @Test
    fun `건너뛰기 줄은 방향 부호를 쓴다`() {
        assertEquals(
            listOf("skip +1 → s=2", "skip -1 → s=1"),
            listOf(ReaderLog.Skipped(1, index = 1).line, ReaderLog.Skipped(-1, index = 0).line),
        )
    }

    @Test
    fun `보조 줄 글자는 설계서와 같다`() {
        val lines = listOf(ReaderLog.Armed, ReaderLog.KeptHold, ReaderLog.FocusSeen(AudioFocus.GAIN)).map { it.line }
        assertEquals(listOf("moving → comma armed", "autoResume=off → keep hold", "focus=GAIN"), lines)
    }

    @Test
    fun `기기 로케일이 쉼표 소수점이어도 로그는 점을 쓴다`() {
        val original = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
        try {
            assertEquals("OFF held 0.80s → play s=1", ReaderLog.ResumedAfterOff(800.milliseconds, index = 0).line)
        } finally {
            Locale.setDefault(original)
        }
    }
}
