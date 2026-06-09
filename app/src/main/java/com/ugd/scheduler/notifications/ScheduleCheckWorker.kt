package com.ugd.scheduler.notifications

import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ugd.scheduler.repository.FirebaseRepository
import java.util.Calendar

class ScheduleCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = FirebaseRepository()
        val uid = repository.getCurrentUserId() ?: return Result.success()

        val locationResult = repository.getFacultyLocation()
        locationResult.onSuccess { facultyLocation ->
            // Провери дали сме близу факултетот
            val lastLocation = getLastKnownLocation() ?: return@onSuccess

            val distance = FloatArray(1)
            Location.distanceBetween(
                lastLocation.latitude, lastLocation.longitude,
                facultyLocation.latitude, facultyLocation.longitude,
                distance
            )

            // Ако сме во радиус
            if (distance[0] <= facultyLocation.geofenceRadius) {
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

                        val currentTotalMin = now.get(Calendar.HOUR_OF_DAY) * 60 +
                                now.get(Calendar.MINUTE)

                        val upcomingSubject = subjects
                            .filter { it.day == todayDay }
                            .filter { subject ->
                                val parts = subject.startTime.split(":")
                                if (parts.size == 2) {
                                    val subjectMin = parts[0].toInt() * 60 + parts[1].toInt()
                                    val diff = subjectMin - currentTotalMin
                                    diff in -120..10
                                } else false
                            }
                            .minByOrNull { subject ->
                                val parts = subject.startTime.split(":")
                                parts[0].toInt() * 60 + parts[1].toInt()
                            }

                        upcomingSubject?.let { subject ->
                            val receiver = GeofenceBroadcastReceiver()
                            receiver.sendScheduleNotification(
                                applicationContext,
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
        return Result.success()
    }

    private fun getLastKnownLocation(): android.location.Location? {
        return try {
            val locationManager = applicationContext.getSystemService(
                Context.LOCATION_SERVICE) as android.location.LocationManager
            locationManager.getLastKnownLocation(
                android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(
                    android.location.LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }
}