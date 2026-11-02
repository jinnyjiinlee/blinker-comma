package app.blinkercomma

import android.os.SystemClock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** 엔진 이벤트 시각 — 단조 시계(부팅 후 경과). 벽시계는 쓰지 않는다(시각 변경·재부팅에 흔들림). */
fun monotonicNow(): Duration = SystemClock.elapsedRealtime().milliseconds
