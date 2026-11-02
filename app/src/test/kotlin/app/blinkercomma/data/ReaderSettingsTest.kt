package app.blinkercomma.data

import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * qa_M3 결함 1 재발 방지 — 새 설치 기본값이 다시 켬으로 돌아가면 이 테스트가 깨진다.
 * `ReaderSettings` 본체는 android.content.Context가 있어야 생성돼 여기선 상수만 고정한다
 * (Robolectric 미도입 — 실제 기본값 동작은 에뮬레이터 새 설치 실측으로 확인, docs/progress.md D6).
 */
class ReaderSettingsTest {
    @Test
    fun `자동 재개 기본값은 끔이다`() {
        assertFalse(ReaderSettings.DEFAULT_AUTO_RESUME)
    }
}
