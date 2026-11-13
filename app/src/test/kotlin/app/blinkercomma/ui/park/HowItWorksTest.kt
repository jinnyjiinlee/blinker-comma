package app.blinkercomma.ui.park

import app.blinkercomma.R
import kotlin.test.Test
import kotlin.test.assertEquals

/** P 화면 사용법 3번 줄 — 기본값(자동 재개 끔)에서 「레버가 돌아오면」이라고 쓰면 실제 동작과 어긋난다. */
class HowItWorksTest {
    @Test
    fun `자동 재개 끔이면 핸들 재생 버튼으로 재개한다고 쓴다`() {
        assertEquals(R.string.step_resume_manual_title, resumeStepTitle(isAutoResume = false))
    }

    @Test
    fun `자동 재개 켬이면 레버가 돌아오면 재개한다고 쓴다`() {
        assertEquals(R.string.step_resume_title, resumeStepTitle(isAutoResume = true))
    }
}
