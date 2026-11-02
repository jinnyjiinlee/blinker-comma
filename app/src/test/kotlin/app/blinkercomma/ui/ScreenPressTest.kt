package app.blinkercomma.ui

import app.blinkercomma.engine.reader.HoldReason
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** P 화면 원 버튼 — 설계서 R8(「자동 재개 끔」이면 P의 ▶로 재개)·원칙 5(첫 재생은 운전자가). */
class ScreenPressTest {
    private val at = 3.seconds

    @Test
    fun `멈춤에서 누르면 재생한다`() {
        assertEquals(ReaderEvent.PlayPressed(at, PressSource.SCREEN), ReaderPhase.Stopped.toScreenPress(at))
    }

    @Test
    fun `읽는 중에 누르면 정지한다`() {
        assertEquals(ReaderEvent.PausePressed(at, PressSource.SCREEN), ReaderPhase.Reading().toScreenPress(at))
    }

    @Test
    fun `쉼표 중에 누르면 재생한다`() {
        val holding = ReaderPhase.Holding(HoldReason.SENTENCE_END)
        assertEquals(ReaderEvent.PlayPressed(at, PressSource.SCREEN), holding.toScreenPress(at))
    }

    @Test
    fun `다른 소리에 밀려 멈췄을 때 누르면 재생한다`() {
        assertEquals(ReaderEvent.PlayPressed(at, PressSource.SCREEN), ReaderPhase.FocusPaused.toScreenPress(at))
    }
}
