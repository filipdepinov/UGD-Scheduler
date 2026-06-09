package com.ugd.scheduler.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence
import com.ugd.scheduler.R
import com.ugd.scheduler.UGDSchedulerApp
import com.ugd.scheduler.activities.MainActivity
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            checkAndNotifyUpcomingClass(context)
        }
    }
    fun sendScheduleNotification(
        context: Context,
        subjectName: String,
        startTime: String,
        endTime: String,
        room: String,
        building: String
    ) {
        sendNotification(context, subjectName, startTime, endTime, room, building)
    }

    private fun checkAndNotifyUpcomingClass(context: Context) {
        val repository = FirebaseRepository()
        val uid = repository.getCurrentUserId() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val userResult = repository.getUser(uid)
            userResult.onSuccess { user ->
                val subjectsResult = repository.getSubjectsByKeys(user.selectedSubjects)
                subjectsResult.onSuccess { subjects ->

                    val now = Calendar.getInstance()
                    val todayDay = when (now.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        else -> return@onSuccess
                    }

                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMin = now.get(Calendar.MINUTE)
                    val currentTotalMin = currentHour * 60 + currentMin

                    val upcomingSubject = subjects
                        .filter { it.day == todayDay }
                        .filter { subject ->
                            val parts = subject.startTime.split(":")
                            if (parts.size == 2) {
                                val subjectTotalMin = parts[0].toInt() * 60 + parts[1].toInt()
                                val diff = subjectTotalMin - currentTotalMin
                                // Започнува за 10 мин или помалку, или е во тек
                                diff in -120..10
                            } else false
                        }
                        .minByOrNull { subject ->
                            val parts = subject.startTime.split(":")
                            parts[0].toInt() * 60 + parts[1].toInt()
                        }

                    upcomingSubject?.let { subject ->
                        sendNotification(
                            context,
                            subject.name,
                            subject.startTime,
                            subject.endTime,
                            subject.room,
                            subject.building
                        )
                    }
                }
            }
        }
    }

    private fun sendNotification(
        context: Context,
        subjectName: String,
        startTime: String,
        endTime: String,
        room: String,
        building: String
    ) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "schedule")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, UGDSchedulerApp.CHANNEL_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📚 Наскоро имаш час!")
            .setContentText("$subjectName • $startTime - $endTime")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Предмет: $subjectName\nВреме: $startTime - $endTime\nПросторија: $room, $building")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }
}