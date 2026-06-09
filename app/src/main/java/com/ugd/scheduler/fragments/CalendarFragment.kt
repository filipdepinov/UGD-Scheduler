package com.ugd.scheduler.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.activities.AddEventActivity
import com.ugd.scheduler.adapters.EventAdapter
import com.ugd.scheduler.databinding.FragmentCalendarBinding
import com.ugd.scheduler.models.CalendarEvent
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()
    private var allEvents = listOf<CalendarEvent>()
    private lateinit var adapter: EventAdapter
    private var selectedDateMillis = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter { event ->
            lifecycleScope.launch {
                repository.deleteCalendarEvent(event.id)
                loadEvents()
                Toast.makeText(requireContext(), "Настанот е избришан", Toast.LENGTH_SHORT).show()
            }
        }
        binding.lvEvents.adapter = adapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            selectedDateMillis = cal.timeInMillis
            filterEventsByDate(selectedDateMillis)
        }

        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(requireContext(), AddEventActivity::class.java).apply {
                putExtra("selected_date", selectedDateMillis)
            }
            startActivity(intent)
        }

        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            val result = repository.getCalendarEvents()
            result.fold(
                onSuccess = { events ->
                    allEvents = events
                    if (selectedDateMillis != 0L) {
                        filterEventsByDate(selectedDateMillis)
                    } else {
                        adapter.updateData(events)
                        updateEmptyState(events.isEmpty())
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Грешка при вчитување на настани", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun filterEventsByDate(dateMillis: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val filtered = allEvents.filter { event ->
            val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
            eventCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            eventCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
        }
        adapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.lvEvents.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
