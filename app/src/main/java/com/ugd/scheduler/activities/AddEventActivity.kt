package com.ugd.scheduler.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.databinding.ActivityAddEventBinding
import com.ugd.scheduler.models.CalendarEvent
import com.ugd.scheduler.models.Subject
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val repository = FirebaseRepository()
    private var selectedDateMillis = 0L
    private var allSubjects = listOf<Subject>()
    private var selectedSubjectId = ""
    private var selectedSubjectName = ""

    private val reminderOptions = listOf("Без потсетник", "15 минути пред", "30 минути пред", "1 час пред", "2 часа пред", "1 ден пред")
    private val reminderMinutes = listOf(0, 15, 30, 60, 120, 1440)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Нов настан"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedDateMillis = intent.getLongExtra("selected_date", System.currentTimeMillis())

        setupReminderDropdown()
        setupSubjectSearch()
        setupDateTimeFields()
        loadSubjects()

        binding.btnSave.setOnClickListener { saveEvent() }
    }

    private fun setupReminderDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reminderOptions)
        binding.acReminder.setAdapter(adapter)
        binding.acReminder.setText(reminderOptions[0], false)
    }

    private fun setupSubjectSearch() {
        binding.etSubjectSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSubjects(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDateTimeFields() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val dateStr = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
        binding.tvSelectedDate.text = "Датум: $dateStr"

        binding.tvSelectedDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, day, 0, 0, 0)
                selectedDateMillis = newCal.timeInMillis
                binding.tvSelectedDate.text = "Датум: $day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                binding.tvSelectedTime.text = String.format("%02d:%02d", hour, minute)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
    }

    private fun loadSubjects() {
        lifecycleScope.launch {
            val result = repository.getAllSubjects()
            result.onSuccess { subjects ->
                allSubjects = subjects
            }
        }
    }

    private fun filterSubjects(query: String) {
        if (query.length < 2) {
            binding.lvSubjectSuggestions.visibility = View.GONE
            return
        }
        val filtered = allSubjects.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            binding.lvSubjectSuggestions.visibility = View.GONE
            return
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filtered.map { it.name })
        binding.lvSubjectSuggestions.adapter = adapter
        binding.lvSubjectSuggestions.visibility = View.VISIBLE
        binding.lvSubjectSuggestions.setOnItemClickListener { _, _, position, _ ->
            val subject = filtered[position]
            selectedSubjectId = subject.id
            selectedSubjectName = subject.name
            binding.etSubjectSearch.setText(subject.name)
            binding.lvSubjectSuggestions.visibility = View.GONE
        }
    }

    private fun saveEvent() {
        val title = binding.etEventTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Внеси назив на настанот", Toast.LENGTH_SHORT).show()
            return
        }

        val type = when {
            binding.rbColloquium.isChecked -> "colloquium"
            binding.rbExam.isChecked -> "exam"
            binding.rbDeadline.isChecked -> "deadline"
            else -> "note"
        }

        val timeText = binding.tvSelectedTime.text.toString()
        val time = if (timeText == "Избери време") "" else timeText

        val reminderText = binding.acReminder.text.toString()
        val reminderIdx = reminderOptions.indexOf(reminderText)
        val reminderMin = if (reminderIdx >= 0) reminderMinutes[reminderIdx] else 0

        val event = CalendarEvent(
            title = title,
            type = type,
            subjectId = selectedSubjectId,
            subjectName = selectedSubjectName,
            date = selectedDateMillis,
            time = time,
            reminderMinutes = reminderMin
        )

        lifecycleScope.launch {
            val result = repository.saveCalendarEvent(event)
            result.fold(
                onSuccess = {
                    Toast.makeText(this@AddEventActivity, "Настанот е зачуван!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Toast.makeText(this@AddEventActivity, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
