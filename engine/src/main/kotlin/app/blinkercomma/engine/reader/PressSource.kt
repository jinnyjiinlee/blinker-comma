package app.blinkercomma.engine.reader

/** 재생·정지·건너뛰기를 누른 곳. 핸들 키는 MediaSession 콜백으로 들어온다. */
enum class PressSource(val logName: String) {
    SCREEN("screen"),
    MEDIA_KEY("mediaKey"),
}
