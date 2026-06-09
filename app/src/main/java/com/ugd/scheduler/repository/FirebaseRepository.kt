package com.ugd.scheduler.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ugd.scheduler.models.*
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun register(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) { Result.failure(e) }
    }

    fun logout() = auth.signOut()
    fun getCurrentUserId() = auth.currentUser?.uid
    fun isLoggedIn() = auth.currentUser != null

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getUser(uid: String): Result<User> {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            Result.success(doc.toObject(User::class.java) ?: User())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getAllSubjects(): Result<List<Subject>> {
        return try {
            val snapshot = db.collection("subjects").get().await()
            val subjects = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subject::class.java)?.copy(id = doc.id)
            }
            Result.success(subjects)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getSubjectsByKeys(keys: List<String>): Result<List<Subject>> {
        return try {
            if (keys.isEmpty()) return Result.success(emptyList())
            val snapshot = db.collection("subjects").get().await()
            val subjects = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subject::class.java)?.copy(id = doc.id)
            }.filter { keys.contains(it.subjectKey) }
            Result.success(subjects)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getSubjectsBySmer(smer: String): Result<List<Subject>> {
        return try {
            val snapshot = db.collection("subjects").get().await()
            val subjects = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subject::class.java)?.copy(id = doc.id)
            }.filter { it.smer == smer }
            Result.success(subjects)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun seedSubjectsIfEmpty() {
        val snapshot = db.collection("subjects").limit(1).get().await()
        if (snapshot.isEmpty) {
            for (subject in getSampleSubjects()) {
                db.collection("subjects").add(subject).await()
            }
        }
    }

    suspend fun reseedSubjects() {
        val snapshot = db.collection("subjects").get().await()
        for (doc in snapshot.documents) doc.reference.delete().await()
        for (subject in getSampleSubjects()) {
            db.collection("subjects").add(subject).await()
        }
    }

    suspend fun saveCalendarEvent(event: CalendarEvent): Result<Unit> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            val e = event.copy(userId = uid)
            if (event.id.isEmpty()) db.collection("calendar_events").add(e).await()
            else db.collection("calendar_events").document(event.id).set(e).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getCalendarEvents(): Result<List<CalendarEvent>> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            val snapshot = db.collection("calendar_events").whereEqualTo("userId", uid).get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CalendarEvent::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteCalendarEvent(eventId: String): Result<Unit> {
        return try {
            db.collection("calendar_events").document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getFacultyLocation(): Result<FacultyLocation> {
        return try {
            val snapshot = db.collection("faculty_locations").limit(1).get().await()
            if (!snapshot.isEmpty) {
                Result.success(snapshot.documents[0].toObject(FacultyLocation::class.java) ?: getDefaultLocation())
            } else Result.success(getDefaultLocation())
        } catch (e: Exception) { Result.success(getDefaultLocation()) }
    }

    private fun getDefaultLocation() = FacultyLocation(
        id = "ugd_main", name = "УГД Факултет за Информатика",
        latitude=41.74634535316385 , longitude=22.183924754788205 , geofenceRadius = 150f
    )

    private fun getSampleSubjects(): List<Subject> = listOf(

        // ===== КИТ - СЕМЕСТАР 2 =====
        Subject(subjectKey="kit_2_math2_mon", name="Математика 2", professor="проф. д-р Т.А.Пачемска", day="Monday", startTime="08:00", endTime="10:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_kompel_mon", name="Компјутерски електронски елементи", professor="проф. д-р Д.Стојанов", day="Monday", startTime="10:00", endTime="12:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_oop_tue", name="Објектно-ориентирано програмирање", professor="проф. д-р М.К.Витанова", day="Tuesday", startTime="08:00", endTime="10:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_kompel_tue", name="Компјутерски електронски елементи", professor="проф. д-р З.Златев", day="Tuesday", startTime="10:00", endTime="12:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_oop_wed", name="Објектно-ориентирано програмирање (вежби)", professor="проф. д-р В.М.Банде", day="Wednesday", startTime="08:00", endTime="10:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_math2_wed", name="Математика 2 (вежби)", professor="проф. д-р Ј.В.Буралиева", day="Wednesday", startTime="10:00", endTime="12:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_discmath_thu1", name="Дискретна математика", professor="проф. д-р Л.К.Лазарова", day="Thursday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_discmath_thu2", name="Дискретна математика (вежби)", professor="проф. д-р Л.К.Лазарова", day="Thursday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_2_lang_fri", name="Странски јазик", professor="", day="Friday", startTime="09:00", endTime="11:00", room="Microsoft Teams", building="Онлајн", semester=2, year=1, credits=4, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КИТ - СЕМЕСТАР 4 =====
        Subject(subjectKey="kit_4_opres_mon1", name="Операциони истражувања", professor="проф. д-р А.Крстев", day="Monday", startTime="09:00", endTime="11:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_opres_mon2", name="Операциони истражувања (вежби)", professor="проф. д-р А.Крстев", day="Monday", startTime="11:00", endTime="12:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_os_mon", name="Оперативни системи", professor="проф. д-р З.Златев", day="Monday", startTime="12:00", endTime="14:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_visprog_lab", name="Визуелно програмирање (лаб.)", professor="", day="Tuesday", startTime="09:00", endTime="10:00", room="Предавална 1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_visprog_tue", name="Визуелно програмирање", professor="проф. д-р В.Кокаланов", day="Tuesday", startTime="10:00", endTime="12:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_os_tue", name="Оперативни системи (вежби)", professor="проф. д-р З.Златев", day="Tuesday", startTime="12:00", endTime="14:00", room="Амф.1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_smetacki_tue1", name="Сметачки алатки во инженерството", professor="проф. д-р В.Гичев", day="Tuesday", startTime="14:00", endTime="16:00", room="Институт за информатика", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_smetacki_tue2", name="Сметачки алатки во инженерството (вежби)", professor="проф. д-р В.Гичев", day="Tuesday", startTime="16:00", endTime="17:00", room="Институт за информатика", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_db_wed", name="Бази на податоци", professor="проф. д-р Ц.М.Банде", day="Wednesday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_visprog_wed", name="Визуелно програмирање (предавање)", professor="проф. д-р С.Коцески / В.Кокаланов", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_netw_thu1", name="Компјутерски мрежи", professor="проф. д-р А.Милева", day="Thursday", startTime="08:00", endTime="10:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_netw_thu2", name="Компјутерски мрежи (вежби)", professor="проф. д-р А.Милева", day="Thursday", startTime="10:00", endTime="12:00", room="Предавална 5", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_db_thu1", name="Бази на податоци (вежби)", professor="проф. д-р А.С.Илиевска", day="Thursday", startTime="12:00", endTime="14:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_4_db_lab", name="Бази на податоци (лаб.)", professor="проф. д-р А.С.Илиевска", day="Thursday", startTime="14:00", endTime="15:00", room="Предавална 1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КИТ - СЕМЕСТАР 6 =====
        Subject(subjectKey="kit_6_stat_mon1", name="Вовед во статистичка анализа", professor="проф. д-р Л.К.Лазарова", day="Monday", startTime="08:00", endTime="10:00", room="Институт за информатика", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_stat_mon2", name="Вовед во статистичка анализа (вежби)", professor="проф. д-р Л.К.Лазарова", day="Monday", startTime="10:00", endTime="11:00", room="Институт за информатика", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_arch_mon1", name="Современи компјутерски архитектури", professor="проф. д-р Д.Биков", day="Monday", startTime="11:00", endTime="13:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_arch_mon2", name="Современи компјутерски архитектури (вежби)", professor="проф. д-р Д.Биков", day="Monday", startTime="13:00", endTime="14:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_netos_mon1", name="Мрежни оперативни системи", professor="проф. д-р З.Златев", day="Monday", startTime="14:00", endTime="16:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_netos_mon2", name="Мрежни оперативни системи (вежби)", professor="проф. д-р З.Златев", day="Monday", startTime="16:00", endTime="17:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_theory_tue1", name="Теорија на информации", professor="проф. д-р Н.Стојковиќ", day="Tuesday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_theory_tue2", name="Теорија на информации (вежби)", professor="проф. д-р Н.Стојковиќ", day="Tuesday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_numeric_tue", name="Нумерички методи (вежби)", professor="проф. д-р М.К.Витанова", day="Tuesday", startTime="12:00", endTime="14:00", room="Амф.2", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_numeric_wed", name="Нумерички методи", professor="проф. д-р В.Гичев", day="Wednesday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_micro_wed1", name="Микрокомпјутерски системи", professor="проф. д-р А.С.Илиевска", day="Wednesday", startTime="10:00", endTime="12:00", room="Предавална 5", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_micro_wed2", name="Микрокомпјутерски системи (вежби)", professor="проф. д-р А.С.Илиевска", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 5", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_ikt_fri1", name="Управување со ИКТ проекти", professor="проф. д-р А.Крстев", day="Friday", startTime="08:00", endTime="10:00", room="Предавална 2", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_6_ikt_fri2", name="Управување со ИКТ проекти (вежби)", professor="проф. д-р А.Крстев", day="Friday", startTime="10:00", endTime="12:00", room="Предавална 2", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КИТ - СЕМЕСТАР 8 =====
        Subject(subjectKey="kit_8_embedded_mon1", name="Вградливи компјутерски системи", professor="проф. д-р Д.Биков", day="Monday", startTime="08:00", endTime="10:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_embedded_mon2", name="Вградливи компјутерски системи (вежби)", professor="проф. д-р Д.Биков", day="Monday", startTime="10:00", endTime="11:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_distrib_mon", name="Дистрибуирани компјутерски системи", professor="доц. д-р А.Велинов", day="Monday", startTime="14:00", endTime="16:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_datasci_tue1", name="Вовед во науката за податоците", professor="проф. д-р З.Здравев", day="Tuesday", startTime="10:00", endTime="12:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_datasci_tue2", name="Вовед во науката за податоците (вежби)", professor="доц. д-р А.Велинов", day="Tuesday", startTime="12:00", endTime="13:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_datasci_lab", name="Вовед во науката за податоците (лаб.)", professor="доц. д-р А.Велинов", day="Tuesday", startTime="13:00", endTime="14:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_diffeq_tue1", name="Диференцијални равенки", professor="проф. д-р Б.Златановска", day="Tuesday", startTime="14:00", endTime="16:00", room="Предавална 4", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_diffeq_tue2", name="Диференцијални равенки (вежби)", professor="проф. д-р Б.Златановска", day="Tuesday", startTime="16:00", endTime="17:00", room="Предавална 4", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_distrib_lab", name="Дистрибуирани компјутерски системи (лаб.)", professor="", day="Wednesday", startTime="08:00", endTime="09:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_cloud_wed1", name="Инфраструктура на облак и сервиси", professor="доц. д-р А.Велинов", day="Wednesday", startTime="09:00", endTime="10:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_cloud_wed2", name="Инфраструктура на облак и сервиси (вежби)", professor="доц. д-р А.Велинов", day="Wednesday", startTime="11:00", endTime="12:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_hci_wed", name="Интеракција компјутер-корисник", professor="проф. д-р Н.Коцеска", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_mobile_wed", name="Развој на мобилни апликации", professor="проф. д-р Сашо Коцески", day="Wednesday", startTime="14:00", endTime="16:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kit_8_distrib_thu", name="Дистрибуирани компјутерски системи (предавање)", professor="проф. д-р Доне Стојанов", day="Thursday", startTime="11:00", endTime="13:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КИТ", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КН - СЕМЕСТАР 2 =====
        Subject(subjectKey="kn_2_kompel_mon", name="Компјутерски електронски елементи", professor="проф. д-р Д.Стојанов", day="Monday", startTime="10:00", endTime="12:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_calculus_mon", name="Калкулус 2", professor="проф. д-р М.Митева", day="Monday", startTime="12:00", endTime="14:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_oop_tue", name="Објектно-ориентирано програмирање", professor="проф. д-р М.К.Витанова", day="Tuesday", startTime="08:00", endTime="10:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_kompel_tue", name="Компјутерски електронски елементи (вежби)", professor="проф. д-р З.Златев", day="Tuesday", startTime="10:00", endTime="12:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_calculus_tue", name="Калкулус 2 (вежби)", professor="проф. д-р М.Митева", day="Tuesday", startTime="12:00", endTime="14:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_oop_lab", name="Објектно-ориентирано програмирање (лаб.)", professor="", day="Tuesday", startTime="14:00", endTime="15:00", room="Предавална 1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_oop_wed", name="Објектно-ориентирано програмирање (вежби)", professor="проф. д-р Ц.М.Банде", day="Wednesday", startTime="08:00", endTime="10:00", room="Амф.1", building="Зграда А", semester=2, year=1, credits=8, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_discmath_thu1", name="Дискретна математика", professor="проф. д-р Л.К.Лазарова", day="Thursday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_discmath_thu2", name="Дискретна математика (вежби)", professor="проф. д-р Л.К.Лазарова", day="Thursday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=2, year=1, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_2_lang_fri", name="Странски јазик", professor="", day="Friday", startTime="09:00", endTime="11:00", room="Microsoft Teams", building="Онлајн", semester=2, year=1, credits=4, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КН - СЕМЕСТАР 4 =====
        Subject(subjectKey="kn_4_opres_mon1", name="Операциони истражувања", professor="проф. д-р А.Крстев", day="Monday", startTime="09:00", endTime="11:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_opres_mon2", name="Операциони истражувања (вежби)", professor="проф. д-р А.Крстев", day="Monday", startTime="11:00", endTime="12:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_os_mon", name="Оперативни системи", professor="проф. д-р З.Златев", day="Monday", startTime="12:00", endTime="14:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_algstr_mon1", name="Алгебарски структури", professor="проф. д-р Л.Г.Илиева", day="Monday", startTime="14:00", endTime="16:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_algstr_mon2", name="Алгебарски структури (вежби)", professor="проф. д-р Л.Г.Илиева", day="Monday", startTime="16:00", endTime="17:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_visprog_lab", name="Визуелно програмирање (лаб.)", professor="", day="Tuesday", startTime="09:00", endTime="10:00", room="Предавална 1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_visprog_tue", name="Визуелно програмирање", professor="проф. д-р В.Кокаланов", day="Tuesday", startTime="10:00", endTime="12:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_os_tue", name="Оперативни системи (вежби)", professor="проф. д-р З.Златев", day="Tuesday", startTime="12:00", endTime="14:00", room="Амф.1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_db_wed", name="Бази на податоци", professor="проф. д-р Ц.М.Банде", day="Wednesday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_visprog_wed", name="Визуелно програмирање (предавање)", professor="проф. д-р С.Коцески / В.Кокаланов", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_netw_thu1", name="Компјутерски мрежи", professor="проф. д-р А.Милева", day="Thursday", startTime="08:00", endTime="10:00", room="Предавална 2", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_netw_thu2", name="Компјутерски мрежи (вежби)", professor="проф. д-р А.Милева", day="Thursday", startTime="10:00", endTime="12:00", room="Предавална 5", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_db_thu1", name="Бази на податоци (вежби)", professor="проф. д-р А.С.Илиевска", day="Thursday", startTime="12:00", endTime="14:00", room="Предавална 4", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_4_db_lab", name="Бази на податоци (лаб.)", professor="", day="Thursday", startTime="14:00", endTime="15:00", room="Предавална 1", building="Зграда А", semester=4, year=2, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КН - СЕМЕСТАР 6 =====
        Subject(subjectKey="kn_6_stat_mon1", name="Вовед во статистичка анализа", professor="проф. д-р Л.К.Лазарова", day="Monday", startTime="08:00", endTime="10:00", room="Институт за информатика", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_stat_mon2", name="Вовед во статистичка анализа (вежби)", professor="проф. д-р Л.К.Лазарова", day="Monday", startTime="10:00", endTime="11:00", room="Институт за информатика", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_netconc_mon1", name="Мрежно и конкурентно програмирање", professor="доц. д-р А.Велинов", day="Monday", startTime="11:00", endTime="13:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_netconc_mon2", name="Мрежно и конкурентно програмирање (вежби)", professor="доц. д-р А.Велинов", day="Monday", startTime="13:00", endTime="14:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_distrib_mon", name="Дистрибуирани компјутерски системи", professor="доц. д-р А.Велинов", day="Monday", startTime="14:00", endTime="16:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_theory_tue1", name="Теорија на информации", professor="проф. д-р Н.Стојковиќ", day="Tuesday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_theory_tue2", name="Теорија на информации (вежби)", professor="проф. д-р Н.Стојковиќ", day="Tuesday", startTime="10:00", endTime="12:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_numeric_tue", name="Нумерички методи (вежби)", professor="проф. д-р М.К.Витанова", day="Tuesday", startTime="12:00", endTime="14:00", room="Амф.2", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_numeric_wed", name="Нумерички методи", professor="проф. д-р В.Гичев", day="Wednesday", startTime="08:00", endTime="10:00", room="Предавална 4", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_infosys_wed1", name="Информациски системи", professor="проф. д-р З.Здравев", day="Wednesday", startTime="10:00", endTime="12:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_infosys_wed2", name="Информациски системи (вежби)", professor="проф. д-р З.Здравев", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_swmgmt_wed", name="Управување со софтверски проекти", professor="проф. д-р Н.Коцеска", day="Wednesday", startTime="14:00", endTime="16:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_distrib_lab", name="Дистрибуирани компјутерски системи (лаб.)", professor="", day="Thursday", startTime="10:00", endTime="11:00", room="Предавална 1", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_distrib_thu", name="Дистрибуирани компјутерски системи (предавање)", professor="проф. д-р Доне Стојанов", day="Thursday", startTime="11:00", endTime="13:00", room="Предавална 12", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_infosys_lab", name="Информациски системи (лаб.)", professor="", day="Thursday", startTime="13:00", endTime="14:00", room="Предавална 5", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_6_netconc_lab", name="Мрежно и конкурентно програмирање (лаб.)", professor="", day="Thursday", startTime="14:00", endTime="15:00", room="Предавална 5", building="Зграда А", semester=6, year=3, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),

        // ===== КН - СЕМЕСТАР 8 =====
        Subject(subjectKey="kn_8_modelsim_mon1", name="Моделирање и симулации", professor="проф. д-р Н.Стојковиќ", day="Monday", startTime="09:00", endTime="11:00", room="Предавална 5", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_modelsim_mon2", name="Моделирање и симулации (вежби)", professor="проф. д-р Н.Стојковиќ", day="Monday", startTime="11:00", endTime="12:00", room="Предавална 5", building="Зграда А", semester=8, year=4, credits=6, isRequired=true, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_ml_mon1", name="Машинско учење", professor="проф. д-р Ц.М.Банде", day="Monday", startTime="12:00", endTime="14:00", room="Предавална 5", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_ml_mon2", name="Машинско учење (вежби)", professor="проф. д-р Ц.М.Банде", day="Monday", startTime="14:00", endTime="15:00", room="Предавална 5", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_ml_lab", name="Машинско учење (лаб.)", professor="", day="Tuesday", startTime="08:00", endTime="09:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_iot_tue1", name="Интернет на нештата", professor="доц. д-р А.Велинов", day="Tuesday", startTime="13:00", endTime="15:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_iot_tue2", name="Интернет на нештата (вежби)", professor="доц. д-р А.Велинов", day="Tuesday", startTime="15:00", endTime="16:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_inttrans_wed", name="Интегрални трансформации и примена", professor="проф. д-р Ј.В.Буралиева", day="Wednesday", startTime="08:00", endTime="10:00", room="Институт за информатика", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_hci_wed", name="Интеракција компјутер-корисник", professor="проф. д-р Н.Коцеска", day="Wednesday", startTime="12:00", endTime="14:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_mobile_wed", name="Развој на мобилни апликации", professor="проф. д-р Сашо Коцески", day="Wednesday", startTime="14:00", endTime="16:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_bioinf_thu1", name="Биоинформатика", professor="проф. д-р Д.Стојанов", day="Thursday", startTime="08:00", endTime="10:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_bioinf_thu2", name="Биоинформатика (вежби)", professor="проф. д-р Д.Стојанов", day="Thursday", startTime="10:00", endTime="11:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_iot_lab", name="Интернет на нештата (лаб.)", professor="", day="Thursday", startTime="11:00", endTime="12:00", room="Предавална 1", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_cloudtech_thu1", name="Облак технологии", professor="доц. д-р С.Кордумова", day="Thursday", startTime="13:00", endTime="15:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205),
        Subject(subjectKey="kn_8_cloudtech_thu2", name="Облак технологии (вежби)", professor="доц. д-р С.Кордумова", day="Thursday", startTime="15:00", endTime="16:00", room="Предавална 12", building="Зграда А", semester=8, year=4, credits=6, isRequired=false, smer="КН", latitude=41.74634535316385, longitude=22.183924754788205)
    )
}
