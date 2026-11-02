package app.blinkercomma.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import app.blinkercomma.R

private const val CHANNEL_ID = "reader"

/** 포그라운드 서비스 알림 — 앱 이름 한 줄뿐(문장 본문 0, 원칙 6). 미디어 앱이라 띄우는 알림(HUN)은 쓰지 않는다. */
fun buildReaderNotification(context: Context): Notification {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW),
    )
    return Notification.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .build()
}
