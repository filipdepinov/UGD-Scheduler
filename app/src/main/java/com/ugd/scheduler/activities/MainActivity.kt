package com.ugd.scheduler.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ugd.scheduler.R
import com.ugd.scheduler.databinding.ActivityMainBinding
import com.ugd.scheduler.fragments.*
import com.ugd.scheduler.notifications.GeofenceManager
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.ugd.scheduler.notifications.ScheduleCheckWorker


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var geofenceManager: GeofenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        requestPermissions()

        geofenceManager = GeofenceManager(this)
        geofenceManager.setupGeofence()

        // Стартувај периодична проверка на секои 15 минути
        val workRequest = PeriodicWorkRequestBuilder<ScheduleCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "schedule_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )


        if (savedInstanceState == null) {
            if (intent.getStringExtra("navigate_to") == "schedule") {
                loadFragment(ProfileFragment())
                binding.root.postDelayed({
                    binding.bottomNavigation.selectedItemId = R.id.nav_schedule
                    loadFragment(ScheduleFragment())
                }, 500)
            } else {
                loadFragment(ProfileFragment())
            }

        }


    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("navigate_to") == "schedule") {
            binding.root.postDelayed({
                binding.bottomNavigation.selectedItemId = R.id.nav_schedule
                loadFragment(ScheduleFragment())
            }, 300)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> loadFragment(ProfileFragment())
                R.id.nav_schedule -> loadFragment(ScheduleFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_calendar -> loadFragment(CalendarFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }
}
