package app.blinkercomma.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import app.blinkercomma.engine.signal.CarEvent
import kotlin.time.Duration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Pleos Vehicle SDK 깜빡이·기어 콜백 → `Flow<CarEvent>`.
 * 리스너는 해제하지 않는다 — 하나를 해제하자 같은 앱의 다른 콜백까지 끊겼다(설계서 원칙 9, 에뮬레이터 실측).
 * 서비스가 끝날 때 `Vehicle.release()`가 한꺼번에 정리한다.
 */
class PleosVehicleSource(private val vehicle: Vehicle, private val now: () -> Duration) {
    fun turnSignals(): Flow<CarEvent> = callbackFlow {
        val listener = object : TurnSignalStateListener {
            override fun onTurnSignalStateUpdated(value: TurnSignalValue?) {
                value?.let { trySend(CarEvent.TurnSignalChanged(now(), it.toTurnSignal())) }
            }
            override fun onFailed(e: Exception) {
                close(e)
            }
        }
        vehicle.getTurnSignal().registerTurnSignalState(listener)
        awaitClose()
    }

    fun gears(): Flow<CarEvent> = callbackFlow {
        val listener = object : GearSelectionListener {
            override fun onSelected(value: VehicleGear?) {
                value?.let { trySend(CarEvent.GearChanged(now(), it.toGear())) }
            }
            override fun onFailed(e: Exception) {
                close(e)
            }
        }
        vehicle.getGear().registerGearSelection(listener)
        awaitClose()
    }
}
