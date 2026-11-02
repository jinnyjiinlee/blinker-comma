package app.blinkercomma.ui

import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderPhase
import kotlin.time.Duration

/**
 * P 화면 원 버튼 한 번 = 소리가 나면 정지, 안 나면 재생. 쉼표 중·포커스 멈춤도 「안 남」이라 ▶ —
 * 「자동 재개 끔」이면 P의 ▶가 재개 수단이다(설계서 R8). 핸들 키(MediaSession)와 같은 뜻.
 */
fun ReaderPhase.toScreenPress(at: Duration): ReaderEvent = if (this is ReaderPhase.Reading) {
    ReaderEvent.PausePressed(at, PressSource.SCREEN)
} else {
    ReaderEvent.PlayPressed(at, PressSource.SCREEN)
}
