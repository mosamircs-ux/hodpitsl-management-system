package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

class HospitalRepository(
    private val saasConfigDao: SaaSConfigDao,
    private val patientDao: PatientDao,
    private val doctorDao: DoctorDao,
    private val appointmentDao: AppointmentDao,
    private val pharmacyItemDao: PharmacyItemDao,
    private val billDao: BillDao
) {
    val saasConfigFlow: Flow<SaaSConfigEntity?> = saasConfigDao.getConfigFlow()
    val patientsFlow: Flow<List<PatientEntity>> = patientDao.getAllPatientsFlow()
    val doctorsFlow: Flow<List<DoctorEntity>> = doctorDao.getAllDoctorsFlow()
    val appointmentsFlow: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointmentsFlow()
    val pharmacyItemsFlow: Flow<List<PharmacyItemEntity>> = pharmacyItemDao.getAllItemsFlow()
    val billsFlow: Flow<List<BillEntity>> = billDao.getAllBillsFlow()

    suspend fun getSaaSConfig(): SaaSConfigEntity? = saasConfigDao.getConfig()
    suspend fun insertOrUpdateSaaSConfig(config: SaaSConfigEntity) = saasConfigDao.insertOrUpdateConfig(config)

    // Patient transactions
    suspend fun insertPatient(patient: PatientEntity): Long = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: PatientEntity) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: PatientEntity) = patientDao.deletePatient(patient)
    suspend fun getPatientById(id: Int): PatientEntity? = patientDao.getPatientById(id)

    // Doctor transactions
    suspend fun insertDoctor(doctor: DoctorEntity) = doctorDao.insertDoctor(doctor)
    suspend fun updateDoctor(doctor: DoctorEntity) = doctorDao.updateDoctor(doctor)
    suspend fun deleteDoctor(doctor: DoctorEntity) = doctorDao.deleteDoctor(doctor)

    // Appointment transactions
    suspend fun getAppointmentById(id: Int): AppointmentEntity? = appointmentDao.getAppointmentById(id)
    suspend fun insertAppointment(appointment: AppointmentEntity) = appointmentDao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: AppointmentEntity) = appointmentDao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: AppointmentEntity) = appointmentDao.deleteAppointment(appointment)

    // Pharmacy transactions
    suspend fun insertPharmacyItem(item: PharmacyItemEntity) = pharmacyItemDao.insertItem(item)
    suspend fun updatePharmacyItem(item: PharmacyItemEntity) = pharmacyItemDao.updateItem(item)
    suspend fun deletePharmacyItem(item: PharmacyItemEntity) = pharmacyItemDao.deleteItem(item)

    // Bill transactions
    suspend fun insertBill(bill: BillEntity) = billDao.insertBill(bill)
    suspend fun updateBill(bill: BillEntity) = billDao.updateBill(bill)
    suspend fun deleteBill(bill: BillEntity) = billDao.deleteBill(bill)
}
