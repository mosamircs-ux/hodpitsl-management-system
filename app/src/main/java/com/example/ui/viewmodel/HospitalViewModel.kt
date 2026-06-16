package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.HospitalRepository
import com.example.data.api.GeminiManager
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HospitalRepository(
        database.saasConfigDao(),
        database.patientDao(),
        database.doctorDao(),
        database.appointmentDao(),
        database.pharmacyItemDao(),
        database.billDao()
    )

    // State flows from Repository
    val saasConfig: StateFlow<SaaSConfigEntity?> = repository.saasConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val patients: StateFlow<List<PatientEntity>> = repository.patientsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val doctors: StateFlow<List<DoctorEntity>> = repository.doctorsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appointments: StateFlow<List<AppointmentEntity>> = repository.appointmentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pharmacyItems: StateFlow<List<PharmacyItemEntity>> = repository.pharmacyItemsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bills: StateFlow<List<BillEntity>> = repository.billsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state for Gemini Generation
    private val _isGeneratingSmartDiagnosis = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isGeneratingSmartDiagnosis: StateFlow<Map<Int, Boolean>> = _isGeneratingSmartDiagnosis.asStateFlow()

    // Dashboard computed analytics
    val dashboardRevenue: StateFlow<Double> = bills.map { list ->
        list.filter { it.status == "مدفوع" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardUnpaidRevenue: StateFlow<Double> = bills.map { list ->
        list.filter { it.status == "غير مدفوع" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val inpatientCount: StateFlow<Int> = patients.map { list ->
        list.count { it.isIPD }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outpatientCount: StateFlow<Int> = patients.map { list ->
        list.count { !it.isIPD }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Prepopulate realistic data if SaaS Config is empty
        viewModelScope.launch {
            val configExists = repository.getSaaSConfig()
            if (configExists == null) {
                prepopulateDatabase()
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        val initialConfig = SaaSConfigEntity(
            tenantId = "sh-9821-ksa",
            hospitalName = "مستشفي الشفاء التخصصي السحابي",
            subscriptionPlan = "الذهبية (SaaS Gold)",
            subscriptionStatus = "نشط",
            subscriptionExpiry = "2027-12-31",
            currentDoctorsCount = 4,
            maxDoctorsCount = 15,
            currentPatientsCount = 3,
            totalRevenue = 28540.0
        )
        repository.insertOrUpdateSaaSConfig(initialConfig)

        // Doctors
        val dr1 = DoctorEntity(name = "د. سليمان الرشيد", specialty = "الباطنية والغدد صماء", phone = "0554433221", email = "s.rasheed@shifa.sa", availableDays = "السبت، الإثنين، الأربعاء", status = "متاح")
        val dr2 = DoctorEntity(name = "د. ريم العبدالله", specialty = "طب وجراحة العيون", phone = "0554433222", email = "r.abdallah@shifa.sa", availableDays = "الأحد، الثلاثاء، الخميس", status = "متاح")
        val dr3 = DoctorEntity(name = "د. خالد السبيعي", specialty = "جراحة العظام والمفاصل", phone = "0554433223", email = "k.subaie@shifa.sa", availableDays = "الإثنين، الثلاثاء، الخميس", status = "في عملية")
        val dr4 = DoctorEntity(name = "د. فاطمة الحربي", specialty = "طب الأطفال والرضع", phone = "0554433224", email = "f.harbi@shifa.sa", availableDays = "الأحد، الإثنين، الأربعاء", status = "خارج المناوبة")
        
        repository.insertDoctor(dr1)
        repository.insertDoctor(dr2)
        repository.insertDoctor(dr3)
        repository.insertDoctor(dr4)

        // Patients
        val p1 = PatientEntity(name = "عبدالله بن علي الشهري", age = 45, gender = "ذكر", phone = "0561112223", address = "الرياض، حي الياسمين", bloodGroup = "O+", isIPD = false, roomNumber = null, latestComplaints = "آلام حادة في فم المعدة مع حرقان شديد يزداد بعد تناول الطعام", admissionDate = "2026-06-11")
        val p2 = PatientEntity(name = "سارة بنت محمد العتيبي", age = 29, gender = "أنثى", phone = "0562223334", address = "جدة، حي الروضة", bloodGroup = "A-", isIPD = true, roomNumber = "جناح 304 - سرير أ", latestComplaints = "متابعة ما بعد عملية استئصال الزائدة الدودية بالأمس - حالة مستقرة", admissionDate = "2026-06-10")
        val p3 = PatientEntity(name = "فهد بن عبدالمحسن الدوسري", age = 8, gender = "ذكر", phone = "0563334445", address = "الدمام، حي الشاطئ", bloodGroup = "AB+", isIPD = false, roomNumber = null, latestComplaints = "ارتفاع مفاجئ في درجة الحرارة (39.2) مصحوب برعشة وسعال جاف", admissionDate = "2026-06-11")
        
        val p1Id = repository.insertPatient(p1).toInt()
        val p2Id = repository.insertPatient(p2).toInt()
        val p3Id = repository.insertPatient(p3).toInt()

        // Pharmacy Items
        val ph1 = PharmacyItemEntity(name = "بانادول إكسترا 500 ملج", category = "مسكنات الألم", quantity = 150, price = 12.50, unit = "علبة")
        val ph2 = PharmacyItemEntity(name = "أموكسيسيلين مضاد حيوي", category = "مضادات حيوية", quantity = 85, price = 45.00, unit = "علبة")
        val ph3 = PharmacyItemEntity(name = "فيتامين سي 1000 ملج فوار", category = "فيتامينات مكملة", quantity = 210, price = 18.00, unit = "علبة")
        val ph4 = PharmacyItemEntity(name = "شراب كوفيدين للسعال", category = "أدوية السعال", quantity = 42, price = 24.50, unit = "زجاجة")
        
        repository.insertPharmacyItem(ph1)
        repository.insertPharmacyItem(ph2)
        repository.insertPharmacyItem(ph3)
        repository.insertPharmacyItem(ph4)

        // Bills
        val b1 = BillEntity(patientId = p1Id, patientName = "عبدالله بن علي الشهري", amount = 150.00, date = "2026-06-11", itemsSummary = "كشفية طبية في العيادة العامة", status = "غير مدفوع")
        val b2 = BillEntity(patientId = p2Id, patientName = "سارة بنت محمد العتيبي", amount = 4850.00, date = "2026-06-10", itemsSummary = "رسوم عملية استئصال الزائدة + إقامة جناح يومين مع العلاج", status = "مدفوع")
        val b3 = BillEntity(patientId = p3Id, patientName = "فهد بن عبدالمحسن الدوسري", amount = 95.00, date = "2026-06-11", itemsSummary = "كشفية طوارئ أطفال + خافض حرارة مسكن", status = "مدفوع")
        
        repository.insertBill(b1)
        repository.insertBill(b2)
        repository.insertBill(b3)

        // Appointments
        val ap1 = AppointmentEntity(patientId = p1Id, patientName = "عبدالله بن علي الشهري", doctorId = 1, doctorName = "د. سليمان الرشيد", date = "2026-06-12", time = "10:30 ص", status = "مؤكد", symptoms = "آلام حادة في فم المعدة مع حرقان شديد يزداد بعد تناول الطعام", diagnosisSummary = "توصية الاستشارة السريعة:\n• التشخيص المبدئي: اشتباه في تهيج جدار المعدة أو ارتجاع مريئي.\n• الفحوصات المقترحة: تحاليل دم عامة، فحص مستوى حموضة، سونار للبطن.\n• إرشادات فورية: تجنب الأطعمة المبهرة والدهون، تقسيم الوجبات إلى لقيمات صغيرة.", isSmartSummaryGenerated = true)
        val ap2 = AppointmentEntity(patientId = p3Id, patientName = "فهد بن عبدالمحسن الدوسري", doctorId = 4, doctorName = "د. فاطمة الحربي", date = "2026-06-11", time = "05:00 م", status = "قيد الانتظار", symptoms = "ارتفاع مفاجئ في درجة الحرارة (39.2) مصحوب برعشة وسعال جاف", diagnosisSummary = null, isSmartSummaryGenerated = false)
        
        repository.insertAppointment(ap1)
        repository.insertAppointment(ap2)
    }

    // --- Actions ---

    // Patient Actions
    fun registerNewPatient(name: String, age: Int, gender: String, phone: String, address: String, bloodGroup: String, isIPD: Boolean, roomNumber: String?, latestComplaints: String, admissionDate: String) {
        viewModelScope.launch {
            val patient = PatientEntity(
                name = name,
                age = age,
                gender = gender,
                phone = phone,
                address = address,
                bloodGroup = bloodGroup,
                isIPD = isIPD,
                roomNumber = if (isIPD) roomNumber else null,
                latestComplaints = latestComplaints,
                admissionDate = admissionDate
            )
            repository.insertPatient(patient)
            updateSaaSPatientsCount()
        }
    }

    fun modifyPatient(patient: PatientEntity) {
        viewModelScope.launch {
            repository.updatePatient(patient)
        }
    }

    fun removePatient(patient: PatientEntity) {
        viewModelScope.launch {
            repository.deletePatient(patient)
            updateSaaSPatientsCount()
        }
    }

    // Doctor Actions
    fun registerNewDoctor(name: String, specialty: String, phone: String, email: String, availableDays: String) {
        viewModelScope.launch {
            val doctor = DoctorEntity(
                name = name,
                specialty = specialty,
                phone = phone,
                email = email,
                availableDays = availableDays,
                status = "متاح"
            )
            repository.insertDoctor(doctor)
            updateSaaSDoctorsCount()
        }
    }

    fun modifyDoctor(doctor: DoctorEntity) {
        viewModelScope.launch {
            repository.updateDoctor(doctor)
        }
    }

    fun removeDoctor(doctor: DoctorEntity) {
        viewModelScope.launch {
            repository.deleteDoctor(doctor)
            updateSaaSDoctorsCount()
        }
    }

    // Appointment Actions
    fun bookAppointment(patientId: Int, patientName: String, doctorId: Int, doctorName: String, date: String, time: String, symptoms: String) {
        viewModelScope.launch {
            val appointment = AppointmentEntity(
                patientId = patientId,
                patientName = patientName,
                doctorId = doctorId,
                doctorName = doctorName,
                date = date,
                time = time,
                status = "قيد الانتظار",
                symptoms = symptoms,
                diagnosisSummary = null,
                isSmartSummaryGenerated = false
            )
            repository.insertAppointment(appointment)
        }
    }

    fun modifyAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
        }
    }

    fun updateAppointmentStatus(appointment: AppointmentEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(status = newStatus))
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // Smart AI Diagnostic Action (Gemini)
    fun buildSmartDiagnosisForAppointment(appointment: AppointmentEntity, doctorSpecialty: String) {
        val appointmentId = appointment.id
        if (_isGeneratingSmartDiagnosis.value[appointmentId] == true) return

        viewModelScope.launch {
            _isGeneratingSmartDiagnosis.update { it + (appointmentId to true) }
            try {
                val recommendation = GeminiManager.generateSmartDiagnosis(
                    symptoms = appointment.symptoms,
                    doctorName = appointment.doctorName,
                    specialty = doctorSpecialty
                )
                repository.updateAppointment(
                    appointment.copy(
                        diagnosisSummary = recommendation,
                        isSmartSummaryGenerated = true
                    )
                )
            } catch (e: Exception) {
                Log.e("HospitalVM", "Error in smart diagnostics", e)
            } finally {
                _isGeneratingSmartDiagnosis.update { it + (appointmentId to false) }
            }
        }
    }

    // Pharmacy Actions
    fun addPharmacyItem(name: String, category: String, quantity: Int, price: Double, unit: String) {
        viewModelScope.launch {
            val item = PharmacyItemEntity(
                name = name,
                category = category,
                quantity = quantity,
                price = price,
                unit = unit
            )
            repository.insertPharmacyItem(item)
        }
    }

    fun modifyPharmacyItem(item: PharmacyItemEntity) {
        viewModelScope.launch {
            repository.updatePharmacyItem(item)
        }
    }

    fun deletePharmacyItem(item: PharmacyItemEntity) {
        viewModelScope.launch {
            repository.deletePharmacyItem(item)
        }
    }

    // Billing Actions
    fun generateBill(patientId: Int, patientName: String, amount: Double, date: String, itemsSummary: String, status: String) {
        viewModelScope.launch {
            val bill = BillEntity(
                patientId = patientId,
                patientName = patientName,
                amount = amount,
                date = date,
                itemsSummary = itemsSummary,
                status = status
            )
            repository.insertBill(bill)
            updateSaaSTotalRevenue()
        }
    }

    fun modifyBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.updateBill(bill)
            updateSaaSTotalRevenue()
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            updateSaaSTotalRevenue()
        }
    }

    // SaaS Configuration settings adjustments
    fun updateSaaSPlan(plan: String) {
        viewModelScope.launch {
            val current = repository.getSaaSConfig()
            if (current != null) {
                // Set bounds on staff depending on subscription
                val maxStaff = when(plan) {
                    "الفضية (SaaS Silver)" -> 8
                    "الذهبية (SaaS Gold)" -> 20
                    "البلاتينية (SaaS Platinum)" -> 100
                    else -> 5
                }
                repository.insertOrUpdateSaaSConfig(current.copy(
                    subscriptionPlan = plan,
                    maxDoctorsCount = maxStaff
                ))
            }
        }
    }

    private suspend fun updateSaaSPatientsCount() {
        val current = repository.getSaaSConfig()
        val allPatients = database.patientDao().getAllPatientsFlow().firstOrNull() ?: emptyList()
        if (current != null) {
            repository.insertOrUpdateSaaSConfig(current.copy(currentPatientsCount = allPatients.size))
        }
    }

    private suspend fun updateSaaSDoctorsCount() {
        val current = repository.getSaaSConfig()
        val allDoctors = database.doctorDao().getAllDoctorsFlow().firstOrNull() ?: emptyList()
        if (current != null) {
            repository.insertOrUpdateSaaSConfig(current.copy(currentDoctorsCount = allDoctors.size))
        }
    }

    private suspend fun updateSaaSTotalRevenue() {
        val current = repository.getSaaSConfig()
        val paidAmt = database.billDao().getAllBillsFlow().firstOrNull()?.filter { it.status == "مدفوع" }?.sumOf { it.amount } ?: 0.0
        if (current != null) {
            repository.insertOrUpdateSaaSConfig(current.copy(totalRevenue = paidAmt))
        }
    }
}
