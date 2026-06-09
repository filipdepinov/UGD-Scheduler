package com.ugd.scheduler.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ugd.scheduler.databinding.ActivitySubjectDetailBinding

class SubjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectDetailBinding
    private var latitude = 41.9965
    private var longitude = 22.4975

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val subjectName = intent.getStringExtra("subject_name") ?: ""
        val professor = intent.getStringExtra("professor") ?: ""
        val startTime = intent.getStringExtra("start_time") ?: ""
        val endTime = intent.getStringExtra("end_time") ?: ""
        val room = intent.getStringExtra("room") ?: ""
        val building = intent.getStringExtra("building") ?: ""
        val credits = intent.getIntExtra("credits", 6)
        val semester = intent.getIntExtra("semester", 0)
        val day = intent.getStringExtra("day") ?: ""
        latitude = intent.getDoubleExtra("latitude", 41.9965)
        longitude = intent.getDoubleExtra("longitude", 22.4975)

        supportActionBar?.title = subjectName

        binding.tvSubjectName.text = subjectName
        binding.tvProfessor.text = "👨‍🏫  $professor"
        binding.tvTime.text = "🕐  $startTime - $endTime"
        binding.tvRoom.text = "🚪  Просторија $room, $building"
        binding.tvCredits.text = "⭐  $credits ЕКТС кредити"

        if (semester > 0) binding.tvSemester.text = "📖  Семестар $semester"
        else binding.tvSemester.visibility = android.view.View.GONE

        if (day.isNotEmpty()) {
            val dayMk = when (day) {
                "Monday" -> "Понеделник"
                "Tuesday" -> "Вторник"
                "Wednesday" -> "Среда"
                "Thursday" -> "Четврток"
                "Friday" -> "Петок"
                "Saturday" -> "Сабота"
                else -> day
            }
            binding.tvDay.text = "📅  $dayMk"
        } else {
            binding.tvDay.visibility = android.view.View.GONE
        }

        binding.btnNavigate.setOnClickListener {
            openMapsNavigation()
        }
    }

    private fun openMapsNavigation() {
        try {
            val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}