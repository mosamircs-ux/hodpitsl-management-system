package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HospitalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force RTL Layout to support the Arabic clinical environment beautifully
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        HospitalAppContent(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalAppContent(
    modifier: Modifier = Modifier,
    viewModel: HospitalViewModel = viewModel()
) {
    // Collect reactive States from Room Database ViewModel
    val config by viewModel.saasConfig.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val pharmacyItems by viewModel.pharmacyItems.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()

    // Computed states
    val revenue by viewModel.dashboardRevenue.collectAsStateWithLifecycle()
    val unpaidRevenue by viewModel.dashboardUnpaidRevenue.collectAsStateWithLifecycle()
    val inpatientCount by viewModel.inpatientCount.collectAsStateWithLifecycle()
    val outpatientCount by viewModel.outpatientCount.collectAsStateWithLifecycle()
    val aiGeneratingStatus by viewModel.isGeneratingSmartDiagnosis.collectAsStateWithLifecycle()

    // Active bottom navigation tab index
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state controllers
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var showBookAppointmentDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showAddPharmacyItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = config?.hospitalName ?: "مستشفى الشفاء السحابي",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageOfSaaS(config?.subscriptionPlan),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الباقة ${config?.subscriptionPlan ?: "الذهبية"} - معرف: ${config?.tenantId ?: "SaaS-1"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Refresh/Info */ },
                        modifier = Modifier.testTag("app_info_button")
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "SaaS Sync Status",
                            tint = MedicalPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTextColorForSelectedIcon(),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.People else Icons.Outlined.People, contentDescription = "المرضى") },
                    label = { Text("المرضى", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTextColorForSelectedIcon(),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_patients")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.MedicalServices else Icons.Outlined.MedicalServices, contentDescription = "الأطباء") },
                    label = { Text("الأطباء", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTextColorForSelectedIcon(),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_doctors")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth, contentDescription = "المواعيد") },
                    label = { Text("المواعيد", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTextColorForSelectedIcon(),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_appointments")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(if (selectedTab == 4) Icons.Default.MonetizationOn else Icons.Outlined.MonetizationOn, contentDescription = "الصيدلية والفوترة") },
                    label = { Text("المالية والدوائية", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTextColorForSelectedIcon(),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_finance")
                )
            }
        },
        floatingActionButton = {
            // Context sensitive FAB
            when (selectedTab) {
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddPatientDialog = true },
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = "تسجيل مريض") },
                        text = { Text("تسجيل مريض") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_patient")
                    )
                }
                2 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddDoctorDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = "إضافة طبيب") },
                        text = { Text("تسجيل طبيب") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_doctor")
                    )
                }
                3 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showBookAppointmentDialog = true },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = "حجز موعد") },
                        text = { Text("حجز موعد جديد") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_appointment")
                    )
                }
                4 -> {
                    Column(horizontalAlignment = Alignment.End) {
                        SmallFloatingActionButton(
                            onClick = { showAddPharmacyItemDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(bottom = 8.dp).testTag("fab_add_pharmacy")
                        ) {
                            Icon(Icons.Default.LocalPharmacy, contentDescription = "إضافة دواء")
                        }
                        ExtendedFloatingActionButton(
                            onClick = { showAddBillDialog = true },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "إنشاء فاتورة") },
                            text = { Text("إنشاء فاتورة") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_add_bill")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Main views switcher
            when (selectedTab) {
                0 -> DashboardScreen(
                    config = config,
                    patientsCount = patients.size,
                    doctorsCount = doctors.size,
                    appointmentsCount = appointments.size,
                    revenue = revenue,
                    unpaidRevenue = unpaidRevenue,
                    inpatients = inpatientCount,
                    outpatients = outpatientCount,
                    onPlanChange = { newPlan -> viewModel.updateSaaSPlan(newPlan) }
                )
                1 -> PatientsScreen(
                    patients = patients,
                    onDelete = { viewModel.removePatient(it) },
                    onEdit = { viewModel.modifyPatient(it) }
                )
                2 -> DoctorsScreen(
                    doctors = doctors,
                    onDelete = { viewModel.removeDoctor(it) },
                    onStatusUpdate = { doctor, newStatus ->
                        viewModel.modifyDoctor(doctor.copy(status = newStatus))
                    }
                )
                3 -> AppointmentsScreen(
                    appointments = appointments,
                    doctors = doctors,
                    patients = patients,
                    aiGeneratingStatus = aiGeneratingStatus,
                    onTriggerAI = { appointment, specialty ->
                        viewModel.buildSmartDiagnosisForAppointment(appointment, specialty)
                    },
                    onDelete = { viewModel.deleteAppointment(it) },
                    onStatusUpdate = { appointment, status ->
                        viewModel.updateAppointmentStatus(appointment, status)
                    }
                )
                4 -> FinancePharmacyScreen(
                    pharmacyItems = pharmacyItems,
                    bills = bills,
                    patients = patients,
                    onDeletePharmacy = { viewModel.deletePharmacyItem(it) },
                    onToggleBillStatus = { bill ->
                        val newStatus = if (bill.status == "مدفوع") "غير مدفوع" else "مدفوع"
                        viewModel.modifyBill(bill.copy(status = newStatus))
                    },
                    onDeleteBill = { viewModel.deleteBill(it) },
                    onEditPharmacy = { viewModel.modifyPharmacyItem(it) }
                )
            }
        }
    }

    // --- Dialog Popups ---

    if (showAddPatientDialog) {
        AddPatientDialog(
            onDismiss = { showAddPatientDialog = false },
            onConfirm = { name, age, gender, phone, address, bg, isIpd, room, complaints, date ->
                viewModel.registerNewPatient(
                    name = name,
                    age = age,
                    gender = gender,
                    phone = phone,
                    address = address,
                    bloodGroup = bg,
                    isIPD = isIpd,
                    roomNumber = room,
                    latestComplaints = complaints,
                    admissionDate = date
                )
                showAddPatientDialog = false
            }
        )
    }

    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onConfirm = { name, specialty, phone, email, days ->
                viewModel.registerNewDoctor(
                    name = name,
                    specialty = specialty,
                    phone = phone,
                    email = email,
                    availableDays = days
                )
                showAddDoctorDialog = false
            }
        )
    }

    if (showBookAppointmentDialog) {
        BookAppointmentDialog(
            patients = patients,
            doctors = doctors,
            onDismiss = { showBookAppointmentDialog = false },
            onConfirm = { patientId, patientName, doctorId, doctorName, date, time, symptoms ->
                viewModel.bookAppointment(
                    patientId = patientId,
                    patientName = patientName,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    date = date,
                    time = time,
                    symptoms = symptoms
                )
                showBookAppointmentDialog = false
            }
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            patients = patients,
            onDismiss = { showAddBillDialog = false },
            onConfirm = { patientId, name, amt, date, desc, isPaid ->
                val status = if (isPaid) "مدفوع" else "غير مدفوع"
                viewModel.generateBill(
                    patientId = patientId,
                    patientName = name,
                    amount = amt,
                    date = date,
                    itemsSummary = desc,
                    status = status
                )
                showAddBillDialog = false
            }
        )
    }

    if (showAddPharmacyItemDialog) {
        AddPharmacyItemDialog(
            onDismiss = { showAddPharmacyItemDialog = false },
            onConfirm = { name, cat, qty, price, unit ->
                viewModel.addPharmacyItem(name, cat, qty, price, unit)
                showAddPharmacyItemDialog = false
            }
        )
    }
}

// --- SUB-SCREEN COMPOSABLES ---

@Composable
fun DashboardScreen(
    config: SaaSConfigEntity?,
    patientsCount: Int,
    doctorsCount: Int,
    appointmentsCount: Int,
    revenue: Double,
    unpaidRevenue: Double,
    inpatients: Int,
    outpatients: Int,
    onPlanChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مرحباً بك في لوحة تحكم Smart Hospital SaaS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نظام إدارة المشافي السحابي المتكامل متعدد المستأجرين لإحصائيات العمليات، جدولة المرضى ومتابعة الأدوية والفوترة.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Analytical Statistics Grid (SaaS KPIs)
        item {
            Text(text = "مؤشرات الأداء التشغيلي (SaaS KPIs)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = "المرضى الخارجيين (OPD)",
                        value = outpatients.toString(),
                        bgColor = Color(0xFFE0F2FE),
                        textColor = Color(0xFF0369A1),
                        icon = Icons.Default.DirectionsRun,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "المرضى الداخليين (IPD)",
                        value = inpatients.toString(),
                        bgColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFFB45309),
                        icon = Icons.Default.Hotel,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = "إجمالي التحصيل المالي",
                        value = "${String.format("%,.2f", revenue)} ريال",
                        bgColor = Color(0xFFD1FAE5),
                        textColor = Color(0xFF047857),
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "الذمم المستحقة (غير مسددة)",
                        value = "${String.format("%,.2f", unpaidRevenue)} ريال",
                        bgColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFB91C1C),
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Clinical Counters Info
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiniInfoBubble(label = "المرضى", value = patientsCount.toString(), icon = Icons.Default.Person)
                    MiniInfoBubble(label = "أطباء مسجلين", value = "$doctorsCount/${config?.maxDoctorsCount ?: 15}", icon = Icons.Default.MedicalInformation)
                    MiniInfoBubble(label = "مواعيد مجدولة", value = appointmentsCount.toString(), icon = Icons.Default.Schedule)
                }
            }
        }

        // Smart Insights Engine Panel
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MedicalPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نظام المساعدة السريري الذكي بنقرة واحدة (Gemini 3.5)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "بإمكان الأطباء توليد استشارات وإشعارات تخصصية أولية مساعدة مبنية على شكوى المريض المباشرة وأعراضه المسجلة عبر زر المواعيد.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // SaaS Tenant Plan Configurator
        item {
            Text(text = "إعدادات اشتراك SaaS للمنشأة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "باقة المستأجر النشطة الحالية", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = config?.subscriptionPlan ?: "الذهبية (SaaS Gold)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MedicalTextColorForPlan()
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "الحالة الأساسية", fontSize = 11.sp, color = Color.Gray)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MedicalAccentGreen,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "نشط ومؤمن",
                                    fontSize = 11.sp,
                                    color = MedicalTextSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "محاكاة تعديل مستوى اشتراك المنشأة (SaaS Package Scale)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("الفضية (SaaS Silver)" to "حد 8 أطباء", "الذهبية (SaaS Gold)" to "حد 20 طبيب", "البلاتينية (SaaS Platinum)" to "حد 100 طبيب").forEach { (planName, subtitle) ->
                            val isSelected = config?.subscriptionPlan == planName
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onPlanChange(planName) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = planName.substringAfter("(").substringBefore(")"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientsScreen(
    patients: List<PatientEntity>,
    onDelete: (PatientEntity) -> Unit,
    onEdit: (PatientEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPatients = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients else {
            patients.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patients_screen")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = MedicalPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "إدارة وسجلات المرضى", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "تسجيل ومتابعة تفاصيل حالات المرضى، مع تحديد نوع الدخول (طوارئ/عيادات خارجية أو تنويم داخلي).", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث باسم المريض أو رقم الهاتف...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_patients_input"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredPatients.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.GroupOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "لا توجد سجلات مرضى تتطابق مع بحثك.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPatients) { patient ->
                    PatientCard(
                        patient = patient,
                        onDelete = { onDelete(patient) },
                        onEdit = { onEdit(patient) }
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorsScreen(
    doctors: List<DoctorEntity>,
    onDelete: (DoctorEntity) -> Unit,
    onStatusUpdate: (DoctorEntity, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("doctors_screen")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Healing, contentDescription = null, tint = MedicalPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "طواقم العيادات والأطباء والأقسام", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "تنظيم الأطباء المسجلين وتخصصاتهم الطبية ومواعيد تواجدهم، والتحكم الفوري في حالتهم التشغيلية داخل المستشفى.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        if (doctors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "لم يتم تسجيل أي طبيب في قاعدة البيانات بعد.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(doctors) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        onDelete = { onDelete(doctor) },
                        onStatusToggle = {
                            val next = when (doctor.status) {
                                "متاح" -> "في عملية"
                                "في عملية" -> "خارج المناوبة"
                                else -> "متاح"
                            }
                            onStatusUpdate(doctor, next)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentsScreen(
    appointments: List<AppointmentEntity>,
    doctors: List<DoctorEntity>,
    patients: List<PatientEntity>,
    aiGeneratingStatus: Map<Int, Boolean>,
    onTriggerAI: (AppointmentEntity, String) -> Unit,
    onDelete: (AppointmentEntity) -> Unit,
    onStatusUpdate: (AppointmentEntity, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("appointments_screen")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MedicalPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "جدولة المواعيد والاستشارات الذكية", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "إدارة حجوزات الكشف وتفعيل الاستشارة الطبية الفورية والتحليل الأولي المرضي بالذكاء الاصطناعي (Gemini).", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "لا توجد مواعيد مضافة حالياً. احجز موعداً جديداً بالأسفل.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(appointments) { appointment ->
                    // Find doctor's specialty
                    val matchedDoc = doctors.firstOrNull { it.id == appointment.doctorId }
                    val specialty = matchedDoc?.specialty ?: "ممارس عام وأخصائي"
                    val isGenerating = aiGeneratingStatus[appointment.id] ?: false

                    AppointmentCard(
                        appointment = appointment,
                        doctorSpecialty = specialty,
                        isGenerativeLoading = isGenerating,
                        onTriggerAI = { onTriggerAI(appointment, specialty) },
                        onDelete = { onDelete(appointment) },
                        onAcceptStatus = { onStatusUpdate(appointment, "مؤكد") },
                        onCompleteStatus = { onStatusUpdate(appointment, "مكتمل") }
                    )
                }
            }
        }
    }
}

@Composable
fun FinancePharmacyScreen(
    pharmacyItems: List<PharmacyItemEntity>,
    bills: List<BillEntity>,
    patients: List<PatientEntity>,
    onDeletePharmacy: (PharmacyItemEntity) -> Unit,
    onToggleBillStatus: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    onEditPharmacy: (PharmacyItemEntity) -> Unit
) {
    var subScreenTab by remember { mutableIntStateOf(0) } // 0: Financials, 1: Pharmacy Items

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("finance_pharmacy_screen")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PriceChange, contentDescription = null, tint = MedicalPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "المالية وصيدلية المستنشى السحابية", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "متابعة كشوف حساب المدفوعات والوصفات الطبية للمرضى المخولين، ومخزون الأدوية الحيوي المتاح.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tab selector
        TabRow(
            selectedTabIndex = subScreenTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = subScreenTab == 0,
                onClick = { subScreenTab = 0 },
                text = { Text("الفواتير والذمم المالية (${bills.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = subScreenTab == 1,
                onClick = { subScreenTab = 1 },
                text = { Text("مخزون الأدوية والصيدلية (${pharmacyItems.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subScreenTab == 0) {
            // Billings list
            if (bills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لم يتم تسجيل فواتير بعد.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bills) { bill ->
                        BillCard(
                            bill = bill,
                            onToggleStatus = { onToggleBillStatus(bill) },
                            onDelete = { onDeleteBill(bill) }
                        )
                    }
                }
            }
        } else {
            // Pharmacy inventory list
            if (pharmacyItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد أصناف دوائية مسجلة في الصيدلية.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pharmacyItems) { item ->
                        PharmacyItemCard(
                            item = item,
                            onDelete = { onDeletePharmacy(item) },
                            onQtyIncrement = { onEditPharmacy(item.copy(quantity = item.quantity + 10)) },
                            onQtyDecrement = {
                                if (item.quantity >= 5) {
                                    onEditPharmacy(item.copy(quantity = item.quantity - 5))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- ITEM CARDS WORK IN UI ---

@Composable
fun KpiCard(
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = textColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = textColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = textColor)
        }
    }
}

@Composable
fun MiniInfoBubble(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MedicalPrimary)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun PatientCard(
    patient: PatientEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = patient.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "رقم الهاتف: ${patient.phone}", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (patient.isIPD) MedicalAccentRed else MedicalAccentGreen
                ) {
                    Text(
                        text = if (patient.isIPD) "تنويم رعاية (IPD) - غرفة ${patient.roomNumber}" else "عيادات خارجي (OPD)",
                        fontSize = 10.sp,
                        color = if (patient.isIPD) MedicalTextCritical else MedicalTextSuccess,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "العمر: ${patient.age} سنة  |  الجنس: ${patient.gender}  |  الفصيلة: ${patient.bloodGroup}", fontSize = 11.sp, color = Color.DarkGray)
                Text(text = "تاريخ الدخول: ${patient.admissionDate}", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(text = "الشكوى السريرية المعقدة والأعراض الحالية:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = patient.latestComplaints, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(contentColor = MedicalSecondary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل الشكوى", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MedicalTextCritical)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("شطب السجل", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorEntity,
    onDelete: () -> Unit,
    onStatusToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Healing, contentDescription = null, tint = MedicalPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = doctor.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = doctor.specialty, fontSize = 12.sp, color = MedicalPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (doctor.status) {
                        "متاح" -> MedicalAccentGreen
                        "في عملية" -> MedicalAccentRed
                        else -> Color(0xFFE2E8F0)
                    },
                    modifier = Modifier.clickable { onStatusToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when (doctor.status) {
                                        "متاح" -> MedicalTextSuccess
                                        "في عملية" -> MedicalTextCritical
                                        else -> Color.DarkGray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = doctor.status,
                            fontSize = 10.sp,
                            color = when (doctor.status) {
                                "متاح" -> MedicalTextSuccess
                                "في عملية" -> MedicalTextCritical
                                else -> Color.DarkGray
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "أيام المناوبة: ${doctor.availableDays}", fontSize = 11.sp, color = Color.DarkGray)
                Text(text = doctor.email, fontSize = 11.sp, color = Color.Gray)
            }
            Text(text = "الهاتف: ${doctor.phone}", fontSize = 11.sp, color = Color.Gray)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onStatusToggle,
                    colors = ButtonDefaults.textButtonColors(contentColor = MedicalSecondary)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تغيير الحالة", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    doctorSpecialty: String,
    isGenerativeLoading: Boolean,
    onTriggerAI: () -> Unit,
    onDelete: () -> Unit,
    onAcceptStatus: () -> Unit,
    onCompleteStatus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = appointment.patientName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "الطبيب: ${appointment.doctorName} (${doctorSpecialty})", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (appointment.status) {
                        "مؤكد" -> MedicalAccentGreen
                        "مكتمل" -> Color(0xFFE0F2FE)
                        "ملغي" -> MedicalAccentRed
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Text(
                        text = appointment.status,
                        fontSize = 11.sp,
                        color = when (appointment.status) {
                            "مؤكد" -> MedicalTextSuccess
                            "مكتمل" -> Color(0xFF0369A1)
                            "ملغي" -> MedicalTextCritical
                            else -> Color.DarkGray
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "التاريخ: ${appointment.date}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Text(text = "الوقت المحدد: ${appointment.time}", fontSize = 11.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "أعراض الشكوى للحجز: ${appointment.symptoms}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            // AI Smart report panel
            if (appointment.isSmartSummaryGenerated && !appointment.diagnosisSummary.isNullOrEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7), // Amber warning tint
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "توصية التشخيص الاسترشادي بالذكاء الاصطناعي (Gemini):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appointment.diagnosisSummary,
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = onTriggerAI,
                    modifier = Modifier.fillMaxWidth().testTag("ai_diagnosis_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    enabled = !isGenerativeLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGenerativeLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MedicalPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري قراءة الملف وتوليد التوصية الطبية...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MedicalPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء تحليل وإرشاد طوارئ ذكي (مساعد Gemini)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appointment.status == "قيد الانتظار") {
                    TextButton(onClick = onAcceptStatus) {
                        Text("تأكيد الموعد", fontSize = 11.sp, color = MedicalTextSuccess, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (appointment.status == "مؤكد") {
                    TextButton(onClick = onCompleteStatus) {
                        Text("التقرير الطبي مكتمل", fontSize = 11.sp, color = MedicalSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "مسح الموعد", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun BillCard(
    bill: BillEntity,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = bill.patientName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "تفاصيل الرسوم: ${bill.itemsSummary}", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (bill.status == "مدفوع") MedicalAccentGreen else MedicalAccentRed,
                    modifier = Modifier.clickable { onToggleStatus() }
                ) {
                    Text(
                        text = bill.status,
                        fontSize = 10.sp,
                        color = if (bill.status == "مدفوع") MedicalTextSuccess else MedicalTextCritical,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القيمة الإجمالية: ${String.format("%,.2f", bill.amount)} ريال",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.status == "مدفوع") MedicalTextSuccess else MedicalTextCritical
                )
                Text(text = "تاريخ الإصدار: ${bill.date}", fontSize = 11.sp, color = Color.Gray)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.textButtonColors(contentColor = MedicalSecondary)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (bill.status == "مدفوع") "تحديد كـ غير مدفوع" else "تحصيل المبلغ الفوري", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "مسح الفاتورة", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun PharmacyItemCard(
    item: PharmacyItemEntity,
    onDelete: () -> Unit,
    onQtyIncrement: () -> Unit,
    onQtyDecrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "الفئة: ${item.category}", fontSize = 11.sp, color = MedicalPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${item.price} ريال / ${item.unit}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (item.quantity < 10) MedicalAccentRed else MedicalAccentGreen
                    ) {
                        Text(
                            text = "المتبقي: ${item.quantity} ${item.unit}",
                            fontSize = 10.sp,
                            color = if (item.quantity < 10) MedicalTextCritical else MedicalTextSuccess,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onQtyDecrement) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "ناقص مخزون")
                    }
                    Text(text = "تحديث المخازن", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = onQtyIncrement) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "زائد مخزون")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "مسح دواء", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// --- POPUP FORMS IMPLEMENTATIONS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String, String, String, Boolean, String?, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var isIPD by remember { mutableStateOf(false) }
    var roomNumber by remember { mutableStateOf("") }
    var complaints by remember { mutableStateOf("") }

    val date = "2026-06-11" // Current Date simulation

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = "تسجيل ملف مريض طبي جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalPrimary)
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المريض الثلاثي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("العمر") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الجوال") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان السكني") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الجنس:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row {
                            listOf("ذكر", "أنثى").forEach { g ->
                                val selected = gender == g
                                FilterChip(
                                    selected = selected,
                                    onClick = { gender = g },
                                    label = { Text(g) },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "فصيلة الدم:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box {
                            LazyRow {
                                items(listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")) { bg ->
                                    val selected = bloodGroup == bg
                                    FilterChip(
                                        selected = selected,
                                        onClick = { bloodGroup = bg },
                                        label = { Text(bg) },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    // Outpatient or Inpatient admission rules
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "دخول تنويم داخلي (IPD)؟", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(checked = isIPD, onCheckedChange = { isIPD = it })
                    }
                }
                if (isIPD) {
                    item {
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("رقم الجناح / السرير المخصص") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = complaints,
                        onValueChange = { complaints = it },
                        label = { Text("أعراض شكوى المريض الحالية بالتفصيل") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val age = ageStr.toIntOrNull() ?: 30
                                    onConfirm(name, age, gender, phone, address, bloodGroup, isIPD, roomNumber.ifBlank { null }, complaints, date)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalPrimary)
                        ) {
                            Text("تأكيد التسجيل")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDoctorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("طبيب عام") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var availableDays by remember { mutableStateOf("الأحد، الثلاثاء، الخميس") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "تسجيل طبيب معالج جديد في الطاقم", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalPrimary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الطبيب ثنائي أو ثلاثي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Specialties fast selector
                Text(text = "التخصص والعيادة الموجه إليها:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(listOf("باطنية وغدد", "أطفال ورضع", "جراحة عامة", "العيون ورمد", "قلب وشرايين", "جراحة عظام")) { s ->
                        val selected = specialty == s
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { specialty = s },
                            label = { Text(s) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف التواصل للعيادة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني للعمل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = availableDays,
                    onValueChange = { availableDays = it },
                    label = { Text("أيام العيادات / المناوبات") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm("د. $name", specialty, phone, email, availableDays)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalPrimary)
                    ) {
                        Text("تسجيل الطبيب بنجاح")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentDialog(
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Int, String, String, String, String) -> Unit
) {
    if (patients.isEmpty() || doctors.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تنبيه حرج", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedicalTextCritical)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يجب تسجيل مريض واحد على الأقل وطبيب واحد على الأقل أولاً لكي تتمكن من جدولة موعد كشفية متكاملة.", textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) { Text("موافق") }
                }
            }
        }
        return
    }

    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var selectedDoctorIndex by remember { mutableIntStateOf(0) }
    var date by remember { mutableStateOf("2026-06-12") }
    var time by remember { mutableStateOf("11:00 ص") }
    var symptoms by remember { mutableStateOf("") }

    val pt = patients.getOrNull(selectedPatientIndex) ?: patients.first()
    val doc = doctors.getOrNull(selectedDoctorIndex) ?: doctors.first()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = "جدولة موعد واستشارة سريرية ذكية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalPrimary)
                }

                item {
                    Text(text = "اختر المريض وطالب الرعاية:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(patients.size) { idx ->
                            val selected = selectedPatientIndex == idx
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedPatientIndex = idx
                                    symptoms = patients[idx].latestComplaints
                                },
                                label = { Text(patients[idx].name) }
                            )
                        }
                    }
                }

                item {
                    Text(text = "الطبيب المعالج والعيادة المتاحة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(doctors.size) { idx ->
                            val selected = selectedDoctorIndex == idx
                            FilterChip(
                                selected = selected,
                                onClick = { selectedDoctorIndex = idx },
                                label = { Text("${doctors[idx].name} (${doctors[idx].specialty})") }
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("تاريخ المقابلة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("الوقت المعتمد") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = { Text("الأعراض والشكوى التفصيلية (لتوليد تشخيص Gemini)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onConfirm(pt.id, pt.name, doc.id, doc.name, date, time, symptoms.ifBlank { "كشف روتيني متكامل" })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalPrimary)
                        ) {
                            Text("جدولة وتأكيد الموعد")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddBillDialog(
    patients: List<PatientEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Double, String, String, Boolean) -> Unit
) {
    if (patients.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تنبيه", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedicalTextCritical)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يجب تسجيل مريض واحد على الأقل لإصدار وتثبيت الفواتير المالية.", textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) { Text("موافق") }
                }
            }
        }
        return
    }

    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var amountStr by remember { mutableStateOf("150") }
    var desc by remember { mutableStateOf("فحوصات طبية عامة وعلاجات") }
    var isPaid by remember { mutableStateOf(false) }

    val pt = patients.getOrNull(selectedPatientIndex) ?: patients.first()
    val date = "2026-06-11"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "إنشاء فاتورة ورسوم علاجية جديدة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalPrimary)

                Text(text = "ابحث واختر المريض المدين:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(patients.size) { idx ->
                        val selected = selectedPatientIndex == idx
                        FilterChip(
                            selected = selected,
                            onClick = { selectedPatientIndex = idx },
                            label = { Text(patients[idx].name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة الرسوم المالية المستحقة (ريال)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("تفاصيل العلاج المسعر / كشف الفروع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "هل تم الاستلام والتحصيل المالي فورياً؟", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isPaid, onCheckedChange = { isPaid = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 150.0
                            onConfirm(pt.id, pt.name, amt, date, desc, isPaid)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalPrimary)
                    ) {
                        Text("تثبيت الفاتورة")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPharmacyItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("مسكنات ألم") }
    var quantityStr by remember { mutableStateOf("50") }
    var priceStr by remember { mutableStateOf("25") }
    var unit by remember { mutableStateOf("علبة") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "إدخال صنف دوائي جديد للصيدلية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalPrimary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الدواء التجاري / العلمي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(text = "تصنيف العقار:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(listOf("مسكنات ألم", "مضادات حيوية", "فيتامينات مكملة", "أدوية السعال", "لقاحات طبية", "أخرى")) { c ->
                        val selected = category == c
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { category = c },
                            label = { Text(c) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("المخزون الأولي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("سعر الوحدة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val qty = quantityStr.toIntOrNull() ?: 50
                                val pr = priceStr.toDoubleOrNull() ?: 25.0
                                onConfirm(name, category, qty, pr, unit)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalPrimary)
                    ) {
                        Text("إضافة الصنف الدوائي")
                    }
                }
            }
        }
    }
}

// --- UTIL HELPER METHODS ---

fun imageOfSaaS(plan: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (plan) {
        "البلاتينية (SaaS Platinum)" -> Icons.Default.Stars
        "الذهبية (SaaS Gold)" -> Icons.Default.WorkspacePremium
        else -> Icons.Default.OfflineBolt
    }
}

@Composable
fun MedicalTextColorForPlan(): Color {
    return if (isSystemInDarkTheme()) Color(0xFFE2E8F0) else MedicalSecondary
}

@Composable
fun MedicalTextColorForSelectedIcon(): Color {
    return if (isSystemInDarkTheme()) MedicalNeutralDark else Color.White
}
