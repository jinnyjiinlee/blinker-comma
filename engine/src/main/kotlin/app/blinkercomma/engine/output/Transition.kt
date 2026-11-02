package app.blinkercomma.engine.output

/** 리듀서 한 번의 결과 — 다음 상태와 그 사이 나온 효과(나온 순서대로 실행한다). */
data class Transition<S>(val state: S, val effects: List<Effect> = emptyList()) {
    /** 이 결과의 상태로 다음 단계를 이어 돌리고 효과를 순서대로 붙인다. */
    fun then(next: (S) -> Transition<S>): Transition<S> {
        val after = next(state)
        return Transition(after.state, effects + after.effects)
    }
}
