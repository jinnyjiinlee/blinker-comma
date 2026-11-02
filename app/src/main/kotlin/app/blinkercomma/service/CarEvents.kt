package app.blinkercomma.service

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import android.util.Log
import app.blinkercomma.BuildConfig
import app.blinkercomma.data.SpeedPermission
import app.blinkercomma.engine.reader.ReaderEvent
import app.blinkercomma.monotonicNow
import app.blinkercomma.vehicle.CarSpeedSource
import app.blinkercomma.vehicle.PleosVehicleSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

private const val TAG = "CommaSignal"

/**
 * 차 신호 3종(깜빡이·기어 = Pleos, 속도 = AAOS)을 엔진 입력 하나로. 서비스 시작부터 계속 받는다.
 * 한 신호가 실패해도 나머지는 계속 받는다 — 실패한 신호는 엔진에서 null(확인 안 됨)로 남는다(원칙 4).
 */
fun carEvents(context: Context, vehicle: Vehicle, speedPermission: StateFlow<SpeedPermission>): Flow<ReaderEvent> {
    val pleos = PleosVehicleSource(vehicle, ::monotonicNow)
    val speed = CarSpeedSource(context, ::monotonicNow)
    return merge(
        pleos.turnSignals().logFailures("turnSignal"),
        pleos.gears().logFailures("gear"),
        speedsOnceGranted(speed, speedPermission).logFailures("speed"),
    ).onEach { if (BuildConfig.DEBUG) Log.i(TAG, "car $it") }
        .map { ReaderEvent.Car(it) }
}

/** 첫 실행엔 권한이 없다 — P 화면 안내에서 허용되면 그때 구독한다(Pleos 리스너는 그대로, 속도만 늦게). */
private fun speedsOnceGranted(speed: CarSpeedSource, permission: StateFlow<SpeedPermission>) = flow {
    if (permission.value != SpeedPermission.GRANTED) Log.w(TAG, "speed 확인 안 됨 — CAR_SPEED 허용 기다림")
    permission.first { it == SpeedPermission.GRANTED }
    emitAll(speed.speeds())
}

private fun <T> Flow<T>.logFailures(name: String): Flow<T> =
    catch { error -> Log.w(TAG, "$name 확인 안 됨 — 구독 실패", error) }
