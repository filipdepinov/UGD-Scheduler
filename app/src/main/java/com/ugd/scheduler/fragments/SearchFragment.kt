package com.ugd.scheduler.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.activities.SubjectDetailActivity
import com.ugd.scheduler.adapters.SubjectAdapter
import com.ugd.scheduler.databinding.FragmentSearchBinding
import com.ugd.scheduler.models.Subject
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()
    private var allSubjects = listOf<Subject>()
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubjectAdapter { subject ->
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
                putExtra("semester", subject.semester)
                putExtra("day", subject.day)
            }
            startActivity(intent)
        }
        binding.lvSubjects.adapter = adapter

        setupSearch()
        loadAllSubjects()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSubjects(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadAllSubjects() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = repository.getAllSubjects()
            binding.progressBar.visibility = View.GONE
            result.fold(
                onSuccess = { subjects ->
                    allSubjects = subjects.sortedBy { it.name }
                    adapter.updateData(allSubjects)
                    updateEmptyState(allSubjects.isEmpty())
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Грешка при вчитување на предмети", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun filterSubjects(query: String) {
        val filtered = if (query.isEmpty()) {
            allSubjects
        } else {
            allSubjects.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.lvSubjects.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
