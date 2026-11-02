package app.blinkercomma.vehicle

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import app.blinkercomma.engine.signal.CarEvent
import kotlin.time.Duration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * AAOS 차속(m/s) — Pleos Vehicle SDK엔 속도가 없어 Car API로 받는다. Dangerous 권한 CAR_SPEED 필요.
 * 권한이 없으면 등록이 예외 없이 조용히 아무것도 안 줬다(D2 실측) — 그래서 먼저 권한을 보고, 없거나 등록이 거절되면
 * 흐름을 실패로 닫는다(서비스가 「확인 안 됨」으로 남김, 원칙 4). 연속 속성이라 같은 값이 계속 오므로 바뀔 때만 낸다.
 */
class CarSpeedSource(private val context: Context, private val now: () -> Duration) {
    fun speeds(): Flow<CarEvent.SpeedChanged> = callbackFlow {
        requireSpeedPermission()
        val car = Car.createCar(context)
        val manager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
        val callback = speedCallback { trySend(it) }
        if (!manager.registerCallback(callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, RATE)) {
            car.disconnect()
            throw SpeedUnavailable("speed callback refused")
        }
        awaitClose {
            manager.unregisterCallback(callback)
            car.disconnect()
        }
    }.distinctUntilChanged { old, new -> old.speedMps == new.speedMps }

    private fun requireSpeedPermission() {
        if (context.checkSelfPermission(Car.PERMISSION_SPEED) != PackageManager.PERMISSION_GRANTED) {
            throw SpeedUnavailable("CAR_SPEED not granted")
        }
    }

    private fun speedCallback(onSpeed: (CarEvent.SpeedChanged) -> Unit) =
        object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                (value.value as? Float)?.let { onSpeed(CarEvent.SpeedChanged(now(), it.toDouble())) }
            }
            override fun onErrorEvent(propertyId: Int, zone: Int) = Unit
        }

    private companion object {
        const val RATE = CarPropertyManager.SENSOR_RATE_NORMAL
    }
}

/** 속도 신호를 못 받음 — 예상되는 실패(권한 거부·미지원)라 흐름을 닫는 데만 쓴다. */
class SpeedUnavailable(reason: String) : IllegalStateException(reason)
