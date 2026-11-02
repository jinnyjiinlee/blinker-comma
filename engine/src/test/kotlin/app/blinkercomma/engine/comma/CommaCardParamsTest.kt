package app.blinkercomma.engine.comma

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** 설계서 「쉼표 규칙」 표의 숫자 — 문턱을 바꾸면 이 테스트와 설계서를 같이 고친다. */
class CommaCardParamsTest {
    @Test
    fun `쉼표 문턱 기본값은 설계서 쉼표 규칙 표 값이다`() {
        val expected = CommaCardParams(
            offDebounce = 0.8.seconds,
            endWaitLimit = 2.5.seconds,
            movingMinMps = 0.3,
            isAutoResume = true,
        )
        assertEquals(expected, CommaCardParams.DEFAULT)
    }
}
