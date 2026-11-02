package app.blinkercomma.engine.support

import app.blinkercomma.engine.output.Effect
import app.blinkercomma.engine.output.Transition

/** 시간 리듀서 테스트 공통 도우미 — 이벤트를 시각 순으로 넣고 마지막 상태와 그동안 나온 효과 전부를 돌려준다. */
fun <S, E> replay(initial: S, events: List<E>, reduce: (S, E) -> Transition<S>): Transition<S> =
    events.fold(Transition(initial)) { last, event -> last.then { reduce(it, event) } }

/** 나온 로그 줄만. */
val Transition<*>.logs: List<String> get() = effects.filterIsInstance<Effect.WriteLog>().map { it.line }
