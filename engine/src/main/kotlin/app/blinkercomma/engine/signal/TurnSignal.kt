package app.blinkercomma.engine.signal

/** 방향지시등 값. 에뮬레이터는 레버 값을 그대로 주고 점멸 꺼짐이 없다(setup T_results 2026-10-25 T9 실측). */
enum class TurnSignal {
    OFF,
    LEFT,
    RIGHT,
    ;

    /** 레버가 올라가(내려가) 있음 — 왼쪽·오른쪽은 쉼표에서 같은 뜻이다. */
    val isOn: Boolean get() = this != OFF
}
