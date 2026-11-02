package app.blinkercomma.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 시연 주입 표시(설계서 원칙 10). VC로 못 넣는 신호를 디버그 빌드의 주입 수신기로 넣으면 여기 이름이 남고,
 * 화면이 「시연 주입」을 띄운다. 릴리스 빌드엔 수신기가 없어 늘 빈 집합.
 */
class DemoInjectionMarks {
    private val _kinds = MutableStateFlow<Set<String>>(emptySet())
    val kinds: StateFlow<Set<String>> = _kinds.asStateFlow()

    fun mark(kind: String) {
        _kinds.value = _kinds.value + kind
    }
}
