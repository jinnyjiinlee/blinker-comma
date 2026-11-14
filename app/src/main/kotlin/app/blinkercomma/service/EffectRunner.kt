package app.blinkercomma.service

import android.util.Log
import app.blinkercomma.data.PositionStore
import app.blinkercomma.data.TallyStore
import app.blinkercomma.engine.output.Effect
import app.blinkercomma.player.ClipPlayer

/** 엔진 효과 → 재생·멈춤·로그·저장. ⏸ 표시는 세션이 직접 들고 있어 여기 오지 않는다. */
class EffectRunner(
    private val player: ClipPlayer,
    private val focus: AudioFocusGate,
    private val tally: TallyStore,
    private val position: PositionStore,
    private val onPlaying: () -> Unit,
) {
    fun run(effect: Effect) {
        when (effect) {
            is Effect.PlayClip -> {
                focus.hold()
                onPlaying()
                player.play(effect.index, effect.isFromStart)
                position.save(effect.index)
            }
            Effect.PauseClip -> player.pause()
            // 로그 줄은 릴리스에서도 남긴다 — 영상 PiP·실측 근거(실명·위치 없음)
            is Effect.WriteLog -> Log.i(COMMA_TAG, effect.line)
            is Effect.SaveTally -> tally.save(effect.tally)
            is Effect.ShowHoldGlyph -> Unit
        }
    }
}

/** 엔진 로그 줄 태그 — 설계서 「쉼표 규칙」 아래 ⓐ~ⓓ, 대본 anchor가 이 태그를 읽는다. */
const val COMMA_TAG = "Comma"
