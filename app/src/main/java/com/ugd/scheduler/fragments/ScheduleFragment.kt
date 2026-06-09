package com.ugd.scheduler.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.R
import com.ugd.scheduler.activities.SubjectDetailActivity
import com.ugd.scheduler.adapters.ScheduleAdapter
import com.ugd.scheduler.databinding.FragmentScheduleBinding
import com.ugd.scheduler.models.Subject
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()
    private var allSubjects = listOf<Subject>()
    private var selectedDay = ""
    private lateinit var adapter: ScheduleAdapter

    private val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val dayNamesMk = listOf("Пон", "Вто", "Сре", "Чет", "Пет", "Саб")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ScheduleAdapter { subject ->
            val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
                putExtra("subject_name", subject.name)
                putExtra("professor", subject.professor)
                putExtra("start_time", subject.startTime)
                putExtra("end_time", subject.endTime)
                putExtra("room", subject.room)
                putExtra("building", subject.building)
                putExtra("credits", subject.credits)
                putExtra("latitude", subject.latitude)
                putExtra("longitude", subject.longitude)
            }
            startActivity(intent)
        }
        binding.lvSchedule.adapter = adapter

        selectedDay = getTodayDayName()
        setupDayButtons()
        loadSchedule()
    }

    private fun getTodayDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }
    }

    private fun setupDayButtons() {
        val dayButtons = listOf(
            binding.btnMon, binding.btnTue, binding.btnWed,
            binding.btnThu, binding.btnFri, binding.btnSat
        )
        val today = getTodayDayName()
        val todayIndex = dayNames.indexOf(today)

        dayButtons.forEachIndexed { index, button ->
            val label = if (index == todayIndex) "${dayNamesMk[index]}\n(Денес)" else dayNamesMk[index]
            button.text = label
            button.setOnClickListener {
                selectedDay = dayNames[index]
                highlightDay(selectedDay)
                filterByDay()
            }
        }
        highlightDay(selectedDay)
    }

    private fun highlightDay(day: String) {
        val dayButtons = listOf(
            binding.btnMon, binding.btnTue, binding.btnWed,
            binding.btnThu, binding.btnFri, binding.btnSat
        )
        val index = dayNames.indexOf(day)
        dayButtons.forEachIndexed { i, btn ->
            if (i == index) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ugd_red))
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text))
            }
        }
    }

    private fun loadSchedule() {
        if (_binding == null) return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val uid = repository.getCurrentUserId() ?: return@launch
            val userResult = repository.getUser(uid)
            userResult.fold(
                onSuccess = { user ->
                    val subjectsResult = repository.getSubjectsByKeys(user.selectedSubjects)
                    subjectsResult.fold(
                        onSuccess = { subjects ->
                            allSubjects = subjects
                            if (_binding == null) return@fold
                            binding.progressBar.visibility = View.GONE
                            filterByDay()
                        },
                        onFailure = {
                            val allResult = repository.getAllSubjects()
                            allResult.onSuccess { s ->
                                allSubjects = s
                                if (_binding == null) return@onSuccess
                                binding.progressBar.visibility = View.GONE
                                filterByDay()
                            }
                        }
                    )
                },
                onFailure = {
                    if (_binding == null) return@fold
                    binding.progressBar.visibility = View.GONE
                    if (context != null) {
                        Toast.makeText(requireContext(), "Грешка при вчитување", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun filterByDay() {
        if (_binding == null) return
        val filtered = allSubjects.filter { it.day == selectedDay }.sortedBy { it.startTime }
        adapter.updateData(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.lvSchedule.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
