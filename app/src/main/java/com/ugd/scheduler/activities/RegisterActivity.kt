package com.ugd.scheduler.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ugd.scheduler.databinding.ActivityRegisterBinding
import com.ugd.scheduler.models.User
import com.ugd.scheduler.repository.FirebaseRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val repository = FirebaseRepository()
    private var selectedSmer = ""
    private var selectedYear = 0

    private val smers = listOf(
        "КИТ - Компјутерско инженерство и технологии",
        "КН - Компјутерски науки"
    )
    private val years = listOf("1 година", "2 година", "3 година", "4 година")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDropdowns()
        binding.btnRegister.setOnClickListener { doRegister() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupDropdowns() {
        val smerAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, smers)
        binding.acDepartment.setAdapter(smerAdapter)
        binding.acDepartment.setOnItemClickListener { _, _, position, _ ->
            selectedSmer = if (position == 0) "КИТ" else "КН"
            updateSubjectVisibility()
        }

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years)
        binding.acYear.setAdapter(yearAdapter)
        binding.acYear.setOnItemClickListener { _, _, position, _ ->
            selectedYear = position + 1
            updateSubjectVisibility()
        }
    }

    private fun updateSubjectVisibility() {
        if (selectedSmer.isEmpty() || selectedYear == 0) return

        // Сокриј сè
        binding.llKitSem2.visibility = View.GONE
        binding.llKitSem4.visibility = View.GONE
        binding.llKitSem6.visibility = View.GONE
        binding.llKitSem8.visibility = View.GONE
        binding.llKnSem2.visibility = View.GONE
        binding.llKnSem4.visibility = View.GONE
        binding.llKnSem6.visibility = View.GONE
        binding.llKnSem8.visibility = View.GONE

        // Прикажи до избраната година
        if (selectedSmer == "КИТ") {
            if (selectedYear >= 1) binding.llKitSem2.visibility = View.VISIBLE
            if (selectedYear >= 2) binding.llKitSem4.visibility = View.VISIBLE
            if (selectedYear >= 3) binding.llKitSem6.visibility = View.VISIBLE
            if (selectedYear >= 4) binding.llKitSem8.visibility = View.VISIBLE
        } else {
            if (selectedYear >= 1) binding.llKnSem2.visibility = View.VISIBLE
            if (selectedYear >= 2) binding.llKnSem4.visibility = View.VISIBLE
            if (selectedYear >= 3) binding.llKnSem6.visibility = View.VISIBLE
            if (selectedYear >= 4) binding.llKnSem8.visibility = View.VISIBLE
        }
    }

    private fun getSelectedSubjects(): List<String> {
        val selected = mutableListOf<String>()

        if (selectedSmer == "КИТ") {
            // Семестар 2
            if (binding.cbKit2Math2.isChecked) { selected.add("kit_2_math2_mon"); selected.add("kit_2_math2_wed") }
            if (binding.cbKit2Kompel.isChecked) { selected.add("kit_2_kompel_mon"); selected.add("kit_2_kompel_tue") }
            if (binding.cbKit2Oop.isChecked) { selected.add("kit_2_oop_tue"); selected.add("kit_2_oop_wed") }
            if (binding.cbKit2Discmath.isChecked) { selected.add("kit_2_discmath_thu1"); selected.add("kit_2_discmath_thu2") }
            if (binding.cbKit2Lang.isChecked) selected.add("kit_2_lang_fri")
            // Семестар 4
            if (binding.cbKit4Opres.isChecked) { selected.add("kit_4_opres_mon1"); selected.add("kit_4_opres_mon2") }
            if (binding.cbKit4Os.isChecked) { selected.add("kit_4_os_mon"); selected.add("kit_4_os_tue") }
            if (binding.cbKit4Visprog.isChecked) { selected.add("kit_4_visprog_lab"); selected.add("kit_4_visprog_tue"); selected.add("kit_4_visprog_wed") }
            if (binding.cbKit4Db.isChecked) { selected.add("kit_4_db_wed"); selected.add("kit_4_db_thu1"); selected.add("kit_4_db_lab") }
            if (binding.cbKit4Netw.isChecked) { selected.add("kit_4_netw_thu1"); selected.add("kit_4_netw_thu2") }
            if (binding.cbKit4Smetacki.isChecked) { selected.add("kit_4_smetacki_tue1"); selected.add("kit_4_smetacki_tue2") }
            // Семестар 6
            if (binding.cbKit6Theory.isChecked) { selected.add("kit_6_theory_tue1"); selected.add("kit_6_theory_tue2") }
            if (binding.cbKit6Numeric.isChecked) { selected.add("kit_6_numeric_wed"); selected.add("kit_6_numeric_tue") }
            if (binding.cbKit6Micro.isChecked) { selected.add("kit_6_micro_wed1"); selected.add("kit_6_micro_wed2") }
            if (binding.cbKit6Ikt.isChecked) { selected.add("kit_6_ikt_fri1"); selected.add("kit_6_ikt_fri2") }
            if (binding.cbKit6Stat.isChecked) { selected.add("kit_6_stat_mon1"); selected.add("kit_6_stat_mon2") }
            if (binding.cbKit6Arch.isChecked) { selected.add("kit_6_arch_mon1"); selected.add("kit_6_arch_mon2") }
            if (binding.cbKit6Netos.isChecked) { selected.add("kit_6_netos_mon1"); selected.add("kit_6_netos_mon2") }
            // Семестар 8
            if (binding.cbKit8Distrib.isChecked) { selected.add("kit_8_distrib_mon"); selected.add("kit_8_distrib_lab"); selected.add("kit_8_distrib_thu") }
            if (binding.cbKit8Datasci.isChecked) { selected.add("kit_8_datasci_tue1"); selected.add("kit_8_datasci_tue2"); selected.add("kit_8_datasci_lab") }
            if (binding.cbKit8Embedded.isChecked) { selected.add("kit_8_embedded_mon1"); selected.add("kit_8_embedded_mon2") }
            if (binding.cbKit8Diffeq.isChecked) { selected.add("kit_8_diffeq_tue1"); selected.add("kit_8_diffeq_tue2") }
            if (binding.cbKit8Cloud.isChecked) { selected.add("kit_8_cloud_wed1"); selected.add("kit_8_cloud_wed2") }
            if (binding.cbKit8Hci.isChecked) selected.add("kit_8_hci_wed")
            if (binding.cbKit8Mobile.isChecked) selected.add("kit_8_mobile_wed")
        } else {
            // Семестар 2
            if (binding.cbKn2Oop.isChecked) { selected.add("kn_2_oop_tue"); selected.add("kn_2_oop_wed"); selected.add("kn_2_oop_lab") }
            if (binding.cbKn2Kompel.isChecked) { selected.add("kn_2_kompel_mon"); selected.add("kn_2_kompel_tue") }
            if (binding.cbKn2Calculus.isChecked) { selected.add("kn_2_calculus_mon"); selected.add("kn_2_calculus_tue") }
            if (binding.cbKn2Discmath.isChecked) { selected.add("kn_2_discmath_thu1"); selected.add("kn_2_discmath_thu2") }
            if (binding.cbKn2Lang.isChecked) selected.add("kn_2_lang_fri")
            // Семестар 4
            if (binding.cbKn4Opres.isChecked) { selected.add("kn_4_opres_mon1"); selected.add("kn_4_opres_mon2") }
            if (binding.cbKn4Os.isChecked) { selected.add("kn_4_os_mon"); selected.add("kn_4_os_tue") }
            if (binding.cbKn4Visprog.isChecked) { selected.add("kn_4_visprog_lab"); selected.add("kn_4_visprog_tue"); selected.add("kn_4_visprog_wed") }
            if (binding.cbKn4Db.isChecked) { selected.add("kn_4_db_wed"); selected.add("kn_4_db_thu1"); selected.add("kn_4_db_lab") }
            if (binding.cbKn4Netw.isChecked) { selected.add("kn_4_netw_thu1"); selected.add("kn_4_netw_thu2") }
            if (binding.cbKn4Algstr.isChecked) { selected.add("kn_4_algstr_mon1"); selected.add("kn_4_algstr_mon2") }
            // Семестар 6
            if (binding.cbKn6Theory.isChecked) { selected.add("kn_6_theory_tue1"); selected.add("kn_6_theory_tue2") }
            if (binding.cbKn6Numeric.isChecked) { selected.add("kn_6_numeric_wed"); selected.add("kn_6_numeric_tue") }
            if (binding.cbKn6Infosys.isChecked) { selected.add("kn_6_infosys_wed1"); selected.add("kn_6_infosys_wed2"); selected.add("kn_6_infosys_lab") }
            if (binding.cbKn6Netconc.isChecked) { selected.add("kn_6_netconc_mon1"); selected.add("kn_6_netconc_mon2"); selected.add("kn_6_netconc_lab") }
            if (binding.cbKn6Distrib.isChecked) { selected.add("kn_6_distrib_mon"); selected.add("kn_6_distrib_lab"); selected.add("kn_6_distrib_thu") }
            if (binding.cbKn6Stat.isChecked) { selected.add("kn_6_stat_mon1"); selected.add("kn_6_stat_mon2") }
            if (binding.cbKn6Swmgmt.isChecked) selected.add("kn_6_swmgmt_wed")
            // Семестар 8
            if (binding.cbKn8Modelsim.isChecked) { selected.add("kn_8_modelsim_mon1"); selected.add("kn_8_modelsim_mon2") }
            if (binding.cbKn8Ml.isChecked) { selected.add("kn_8_ml_mon1"); selected.add("kn_8_ml_mon2"); selected.add("kn_8_ml_lab") }
            if (binding.cbKn8Iot.isChecked) { selected.add("kn_8_iot_tue1"); selected.add("kn_8_iot_tue2"); selected.add("kn_8_iot_lab") }
            if (binding.cbKn8Inttrans.isChecked) selected.add("kn_8_inttrans_wed")
            if (binding.cbKn8Hci.isChecked) selected.add("kn_8_hci_wed")
            if (binding.cbKn8Mobile.isChecked) selected.add("kn_8_mobile_wed")
            if (binding.cbKn8Bioinf.isChecked) { selected.add("kn_8_bioinf_thu1"); selected.add("kn_8_bioinf_thu2") }
            if (binding.cbKn8Cloudtech.isChecked) { selected.add("kn_8_cloudtech_thu1"); selected.add("kn_8_cloudtech_thu2") }
        }
        return selected
    }

    private fun doRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val indexNumber = binding.etIndexNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() ||
            email.isEmpty() || indexNumber.isEmpty() ||
            selectedSmer.isEmpty() || selectedYear == 0) {
            Toast.makeText(this, "Пополни ги сите задолжителни полиња", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Лозинките не се совпаѓаат!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Лозинката мора да има минимум 6 карактери", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSubjects = getSelectedSubjects()
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            val registerResult = repository.register(email, password)
            registerResult.fold(
                onSuccess = { uid ->
                    val user = User(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        indexNumber = indexNumber,
                        email = email,
                        department = "Факултет за Информатика",
                        smer = selectedSmer,
                        studyYear = selectedYear,
                        selectedSubjects = selectedSubjects
                    )
                    repository.saveUser(user)
                    repository.seedSubjectsIfEmpty()
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                },
                onFailure = {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this@RegisterActivity, "Грешка: ${it.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}