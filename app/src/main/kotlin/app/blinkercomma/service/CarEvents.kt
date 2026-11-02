package app.blinkercomma.service

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import android.util.Log
import app.blinkercomma.BuildConfig
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.monotonicNow
import app.blinkercomma.vehicle.CarSpeedSource
import app.blinkercomma.vehicle.PleosVehicleSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

private const val TAG = "CommaSignal"

/**
 * 차 신호 3종(깜빡이·기어 = Pleos, 속도 = AAOS)을 엔진 입력 하나로. 서비스 시작부터 계속 받는다.
 * 한 신호가 실패해도 나머지는 계속 받는다 — 실패한 신호는 엔진에서 null(확인 안 됨)로 남는다(원칙 4).
 */
fun carEvents(context: Context, vehicle: Vehicle): Flow<ReaderEvent> {
    val pleos = PleosVehicleSource(vehicle, ::monotonicNow)
    val speed = CarSpeedSource(context, ::monotonicNow)
    return merge(
        pleos.turnSignals().logFailures("turnSignal"),
        pleos.gears().logFailures("gear"),
        speed.speeds().logFailures("speed"),
    ).onEach { if (BuildConfig.DEBUG) Log.i(TAG, "car $it") }
        .map { ReaderEvent.Car(it) }
}

private fun <T> Flow<T>.logFailures(name: String): Flow<T> =
    catch { error -> Log.w(TAG, "$name 확인 안 됨 — 구독 실패", error) }
