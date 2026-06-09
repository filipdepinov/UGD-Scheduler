package com.ugd.scheduler.models

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val indexNumber: String = "",
    val email: String = "",
    val department: String = "",
    val smer: String = "",
    val studyYear: Int = 1,
    val selectedSubjects: List<String> = emptyList(),
    val profileImageUrl: String = ""
)

data class Subject(
    val id: String = "",
    val subjectKey: String = "",
    val name: String = "",
    val professor: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
    val building: String = "",
    val semester: Int = 1,
    val year: Int = 1,
    val credits: Int = 6,
    val isRequired: Boolean = true,
    val smer: String = "",
    val latitude: Double = 41.9965,
    val longitude: Double = 22.4975
)

data class CalendarEvent(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "note",
    val subjectId: String = "",
    val subjectName: String = "",
    val date: Long = 0L,
    val time: String = "",
    val reminderMinutes: Int = 0
)

data class FacultyLocation(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geofenceRadius: Float = 100f
)
