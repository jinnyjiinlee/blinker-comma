package app.blinkercomma.engine.output

import app.blinkercomma.engine.tally.ReadingTally

/** 엔진 출력 전부. 앱은 이것만 보고 재생·멈춤·표시·기록한다 — 판정은 이미 끝난 상태. */
sealed interface Effect {
    /** `isFromStart = false`면 멈춘 자리에서 이어 재생(포커스 복귀). 쉼표 뒤 재개는 늘 문장 처음부터. */
    data class PlayClip(val index: Int, val isFromStart: Boolean) : Effect

    data object PauseClip : Effect

    /** D 화면 쉼표 표시(⏸) 하나. 소리가 멎는 순간 켜고 다시 날 때 끈다. */
    data class ShowHoldGlyph(val isOn: Boolean) : Effect

    /** 로그 한 줄(태그 `Comma`) — 영상 PiP·실측 근거. 글자는 `toLogLine`이 만든다. */
    data class WriteLog(val line: String) : Effect

    /** 오늘 카운트 저장. 날짜 키는 앱이 붙인다(엔진은 날짜를 모름). */
    data class SaveTally(val tally: ReadingTally) : Effect
}
