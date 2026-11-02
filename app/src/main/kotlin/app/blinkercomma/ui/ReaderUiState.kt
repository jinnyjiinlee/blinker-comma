package app.blinkercomma.ui

import app.blinkercomma.engine.reader.ReaderPhase
import app.blinkercomma.engine.reader.ReaderState

/** 임시 P 화면이 그리는 것 전부(D3에서 P·D·권한 화면으로 나뉜다). */
data class ReaderUiState(
    val isPlaying: Boolean,
    val sentences: Int,
    val commas: Int,
    val isAutoResume: Boolean,
    val isHoldGlyphOn: Boolean,
    val isDemoInjected: Boolean,
)

fun toUiState(state: ReaderState, isAutoResume: Boolean, isHoldGlyphOn: Boolean, isDemoInjected: Boolean) =
    ReaderUiState(
        isPlaying = state.phase != ReaderPhase.Stopped,
        sentences = state.tally.sentences,
        commas = state.tally.commas,
        isAutoResume = isAutoResume,
        isHoldGlyphOn = isHoldGlyphOn,
        isDemoInjected = isDemoInjected,
    )
