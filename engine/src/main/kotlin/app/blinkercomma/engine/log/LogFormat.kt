package app.blinkercomma.engine.log

import app.blinkercomma.engine.output.Effect
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit

/** 로그 한 줄 효과. 글자는 [ReaderLog.line]이 정한다. */
fun toWriteLog(entry: ReaderLog): Effect.WriteLog = Effect.WriteLog(entry.line)

/** `0.80s` — 소수 둘째 자리, 기기 로케일과 무관(로그를 대본·테스트가 같이 읽음). */
internal fun formatSeconds(duration: Duration): String =
    String.format(Locale.ROOT, "%.2fs", duration.toDouble(DurationUnit.SECONDS))

/** `0`·`8.33` — 끝자리 0은 지운다. */
internal fun formatDecimal(value: Double): String =
    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

/** 로그의 문장 번호는 1부터 — 팩 파일 번호 `s01`과 같게. */
internal fun sentenceLabel(index: Int): String = "s=${index + 1}"
