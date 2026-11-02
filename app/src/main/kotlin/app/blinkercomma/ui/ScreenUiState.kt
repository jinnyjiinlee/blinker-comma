package app.blinkercomma.ui

/** 화면이 그리는 것 전부 — 장마다 레이아웃이 달라 sealed. 판정은 [toUiState], 화면은 그리기만. */
sealed interface ScreenUiState {
    /** 정차(기어 P 또는 기어 확인 안 됨) — 조작은 전부 여기서(설계서 원칙 1). */
    data class Park(
        val playback: Playback,
        /** 지금(또는 다음) 문장 번호, 1부터. 팩이 비면 0. */
        val sentenceNumber: Int,
        val sentenceCount: Int,
        val todaySentences: Int,
        val todayCommas: Int,
        val isAutoResume: Boolean,
        /** 못 받은 신호 — 정상이면 빈 목록이라 줄도 안 보인다(원칙 4). */
        val missingSignals: List<MissingSignal>,
        /** 팩 제목(≤6자)·출처 크레딧 — 팩 JSON 글자 그대로. */
        val packTitle: String,
        val credit: String,
        val isDemoInjected: Boolean,
    ) : ScreenUiState

    /**
     * 주행(D·R·N 또는 이동 중) — 글자 0 · 터치 0, 쉼표 중에만 ⏸ 하나(원칙 1).
     * [todayCommas]는 쉼표 중에만 「쉼표 N」 배지로 쓰인다(M8 upgrade_plan 4-2, 원칙 1의 유일한 의도적 예외).
     */
    data class Drive(
        val isHoldGlyphOn: Boolean,
        val holdSide: HoldSide,
        val todayCommas: Int,
        val isDemoInjected: Boolean,
    ) : ScreenUiState

    /** 첫 실행 속도 권한 안내 — 정차에서만, 한 번만. */
    data object AskSpeedPermission : ScreenUiState
}

/** 재생 판에 보이는 낭독 단계. */
enum class Playback { STOPPED, READING, HOLDING, FOCUS_PAUSED }

/** 「확인 안 됨」으로 보일 수 있는 신호. 깜빡이는 첫 콜백 전엔 OFF와 구별이 안 돼 넣지 않는다. */
enum class MissingSignal { GEAR, SPEED }

/** D 화면 ⏸가 들어오는 쪽 — 레버 방향과 같은 쪽 가장자리에서 진입한다(ui_plan.md P0-1). */
enum class HoldSide { LEFT, RIGHT }
