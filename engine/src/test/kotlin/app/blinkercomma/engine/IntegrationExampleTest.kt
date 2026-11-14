package app.blinkercomma.engine

import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.reader.PressSource
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.engine.reader.reduce
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * docs/INTEGRATION.md 1절 그대로 — 다른 오디오 앱이 엔진을 붙이는 법. 앱은 넣고, 받은 Effect대로만 한다.
 * 가짜 플레이어는 「몇 번째 문장을 틀었나」만 기록한다.
 */
class IntegrationExampleTest {
    private class FakeApp(sentences: Int) {
        var state = ReaderState(clipCount = sentences)
        val played = mutableListOf<Int>()
        var isPauseShown = false

        fun feed(event: ReaderEvent) {
            val result = reduce(state, event, CommaCardParams.DEFAULT)
            state = result.state
            result.effects.forEach { effect ->
                when (effect) {
                    is Effect.PlayClip -> played += effect.index
                    Effect.PauseClip -> Unit
                    is Effect.ShowHoldGlyph -> isPauseShown = effect.isOn
                    is Effect.WriteLog, is Effect.SaveTally -> Unit
                }
            }
        }
    }

    @Test
    fun `깜빡이를 켜면 지금 문장 끝에서 쉬고 핸들 키로 다음 문장을 튼다`() {
        val app = FakeApp(sentences = 3)
        app.feed(ReaderEvent.Car(CarEvent.GearChanged(0.seconds, Gear.DRIVE)))
        app.feed(ReaderEvent.Car(CarEvent.SpeedChanged(0.seconds, 11.0)))
        app.feed(ReaderEvent.PlayPressed(1.seconds, PressSource.SCREEN)) // 문장 0
        app.feed(ReaderEvent.Car(CarEvent.TurnSignalChanged(2.seconds, TurnSignal.LEFT)))
        app.feed(ReaderEvent.ClipEnded(3.seconds, index = 0)) // 문장 끝 — 다음 문장을 틀지 않는다
        assertEquals(listOf(0), app.played)
        assertEquals(true, app.isPauseShown)

        app.feed(ReaderEvent.PlayPressed(5.seconds, PressSource.MEDIA_KEY)) // 핸들 재생 키
        assertEquals(listOf(0, 1), app.played)
        assertEquals(false, app.isPauseShown)
    }
}
