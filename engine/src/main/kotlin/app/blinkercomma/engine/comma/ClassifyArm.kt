package app.blinkercomma.engine.comma

import app.blinkercomma.engine.signal.Gear

/**
 * 쉼표를 예약할 순간인가(R1·R3). 속도를 확인 못 하면(권한 거부·미수신) 속도 예외 없이
 * 레버 + D만으로 예약한다 — 설계서 원칙 4.
 */
fun classifyArm(snapshot: CommaSnapshot, params: CommaCardParams = CommaCardParams.DEFAULT): ArmDecision {
    val speedMps = snapshot.speedMps
    return when {
        !snapshot.lever.isOn -> ArmDecision.LEVER_OFF
        snapshot.gear != Gear.DRIVE -> ArmDecision.SKIP_NOT_DRIVE
        speedMps != null && speedMps <= params.movingMinMps -> ArmDecision.SKIP_STOPPED
        else -> ArmDecision.ARMED
    }
}
