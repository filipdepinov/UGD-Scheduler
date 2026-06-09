package com.ugd.scheduler.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.activities.LoginActivity
import com.ugd.scheduler.databinding.FragmentProfileBinding
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()

        // FIX: Logout with confirmation dialog
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Одјава")
                .setMessage("Дали сигурно сакаш да се одјавиш?")
                .setPositiveButton("Да") { _, _ ->
                    repository.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Не", null)
                .show()
        }

        // FIX: Edit profile - show info dialog for now
        binding.btnEditProfile.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Промена на податоци")
                .setMessage("Оваа функција ќе биде достапна наскоро. Во моментот можете да се одјавите и да се регистрирате со нови податоци.")
                .setPositiveButton("Во ред", null)
                .show()
        }
        // TEST копче за GPS нотификација
    }

    private fun loadProfile() {
        val uid = repository.getCurrentUserId() ?: return
        lifecycleScope.launch {
            val result = repository.getUser(uid)
            result.fold(
                onSuccess = { user ->
                    binding.tvFullName.text = "${user.firstName} ${user.lastName}"
                    binding.tvIndexNumber.text = "Индекс: ${user.indexNumber}"
                    binding.tvEmail.text = "✉️  ${user.email}"
                    binding.tvDepartment.text = "🎓  ${user.department}"
                    binding.tvYear.text = "📅  ${user.studyYear} година"
                    val uniqueSubjects = user.selectedSubjects
                        .map { it.substringBeforeLast("_mon").substringBeforeLast("_tue")
                            .substringBeforeLast("_wed").substringBeforeLast("_thu")
                            .substringBeforeLast("_fri").substringBeforeLast("_lab")
                            .substringBeforeLast("_mon1").substringBeforeLast("_mon2")
                            .substringBeforeLast("_tue1").substringBeforeLast("_tue2")
                            .substringBeforeLast("_wed1").substringBeforeLast("_wed2")
                            .substringBeforeLast("_thu1").substringBeforeLast("_thu2")
                            .substringBeforeLast("_fri1").substringBeforeLast("_fri2") }
                        .distinct()
                        .size
                    binding.tvSubjectsCount.text = "📚  Одбрани предмети: $uniqueSubjects"
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Грешка при вчитување на профилот", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
