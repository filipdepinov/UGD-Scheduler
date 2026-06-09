package com.ugd.scheduler

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class UGDSchedulerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE,
                "Распоред на часови",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Нотификации за наближување на часови"
                enableVibration(true)
            }

            val calendarChannel = NotificationChannel(
                CHANNEL_CALENDAR,
                "Потсетници за календар",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Потсетници за испити и настани"
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(scheduleChannel)
            manager.createNotificationChannel(calendarChannel)
        }
    }

    companion object {
        const val CHANNEL_SCHEDULE = "ugd_schedule_channel"
        const val CHANNEL_CALENDAR = "ugd_calendar_channel"
    }
}
