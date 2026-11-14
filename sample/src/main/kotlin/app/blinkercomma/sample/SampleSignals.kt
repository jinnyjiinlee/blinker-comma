package app.blinkercomma.sample

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import app.blinkercomma.engine.signal.CarEvent
import app.blinkercomma.engine.signal.Gear
import app.blinkercomma.engine.signal.TurnSignal
import kotlin.time.Duration

/** Pleos Vehicle SDK 깜빡이·기어 → 엔진 CarEvent. 콜백 스레드에서 오므로 [post]로 메인 스레드에 넘긴다. */
class SampleSignals(
    private val vehicle: Vehicle,
    private val now: () -> Duration,
    private val post: (CarEvent) -> Unit,
) {
    fun start() {
        vehicle.getTurnSignal().registerTurnSignalState(object : TurnSignalStateListener {
            override fun onTurnSignalStateUpdated(value: TurnSignalValue?) {
                value?.let { post(CarEvent.TurnSignalChanged(now(), it.toTurnSignal())) }
            }
            override fun onFailed(e: Exception) = Unit
        })
        vehicle.getGear().registerGearSelection(object : GearSelectionListener {
            override fun onSelected(value: VehicleGear?) {
                value?.let { post(CarEvent.GearChanged(now(), it.toGear())) }
            }
            override fun onFailed(e: Exception) = Unit
        })
    }
}

private fun TurnSignalValue.toTurnSignal(): TurnSignal = when (this) {
    TurnSignalValue.OFF -> TurnSignal.OFF
    TurnSignalValue.LEFT -> TurnSignal.LEFT
    TurnSignalValue.RIGHT -> TurnSignal.RIGHT
}

private fun VehicleGear.toGear(): Gear = when (this) {
    VehicleGear.GEAR_PARK -> Gear.PARK
    VehicleGear.GEAR_REVERSE -> Gear.REVERSE
    VehicleGear.GEAR_NEUTRAL, VehicleGear.GEAR_UNKNOWN -> Gear.NEUTRAL
    else -> Gear.DRIVE
}
