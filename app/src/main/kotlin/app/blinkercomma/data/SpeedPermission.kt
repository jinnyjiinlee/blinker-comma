package app.blinkercomma.data

/** 속도 권한(Dangerous, 런타임). 묻기 전에만 안내 화면을 띄운다 — 거부해도 다시 조르지 않는다. */
enum class SpeedPermission { NOT_ASKED, GRANTED, DENIED }
