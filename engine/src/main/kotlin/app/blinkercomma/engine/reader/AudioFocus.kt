package app.blinkercomma.engine.reader

/** 오디오 포커스 변화. 「줄여도 됨(duck)」은 낭독이라 앱이 LOSS_TRANSIENT로 바꿔 넣는다(말소리는 줄이면 못 알아들음). */
enum class AudioFocus { GAIN, LOSS, LOSS_TRANSIENT }
