package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CMMSViewModel
import com.example.ui.viewmodel.Screen
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMMSAppContainer(viewModel: CMMSViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Listen to UI Events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentUser != null && currentScreen != Screen.LOGIN && currentScreen != Screen.REGISTER) {
                CMMSBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.LOGIN -> LoginScreen(viewModel)
                    Screen.REGISTER -> RegisterScreen(viewModel)
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.ASSETS -> AssetsScreen(viewModel)
                    Screen.MAINTENANCE -> MaintenanceScreen(viewModel)
                    Screen.SPARE_PARTS -> SparePartsScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun CMMSBottomNavigation(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.DASHBOARD,
            onClick = { onNavigate(Screen.DASHBOARD) },
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
            label = { Text("สรุปงาน", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.ASSETS,
            onClick = { onNavigate(Screen.ASSETS) },
            icon = { Icon(Icons.Filled.PrecisionManufacturing, contentDescription = "Assets") },
            label = { Text("เครื่องจักร", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_assets")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.MAINTENANCE,
            onClick = { onNavigate(Screen.MAINTENANCE) },
            icon = { Icon(Icons.Filled.Build, contentDescription = "Maintenance") },
            label = { Text("งานซ่อม", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_maintenance")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.SPARE_PARTS,
            onClick = { onNavigate(Screen.SPARE_PARTS) },
            icon = { Icon(Icons.Filled.SettingsSuggest, contentDescription = "Spare Parts") },
            label = { Text("อะไหล่", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_spare_parts")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onNavigate(Screen.SETTINGS) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("แจ้งเตือน", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}

// 1. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: CMMSViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BluePrimary, Color(0xFF0F172A))
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("login_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Image
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = "Industrial CMMS Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, BluePrimary, RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Industrial CMMS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "ระบบบริหารจัดการงานซ่อมบำรุงอุตสาหกรรม",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("ชื่อผู้ใช้งาน (Username)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("รหัสผ่าน (Password)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                if (loginError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loginError ?: "",
                        color = RedCritical,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Login, contentDescription = "Login icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("เข้าสู่ระบบ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FREE TRIAL BUTTON (Prominent)
                Button(
                    onClick = { viewModel.startFreeTrial() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("free_trial_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAlert),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CardMembership, contentDescription = "Trial Icon", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ทดลองใช้งานฟรี 15 วัน", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ยังไม่มีบัญชีผู้ใช้งาน?", color = Color.Gray, fontSize = 14.sp)
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.REGISTER) },
                        modifier = Modifier.testTag("nav_register_button")
                    ) {
                        Text("สมัครสมาชิกใหม่", color = BluePrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Show default accounts info for testing
                Text(
                    text = "บัญชีทดลองใช้ (Admin):\n• admin / PTP@min_2026\n• niwat.T / 123456",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.DarkGray,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Start
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 2. REGISTRATION SCREEN
@Composable
fun RegisterScreen(viewModel: CMMSViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Technician") }
    val roles = listOf("Manager", "Supervisor", "Technician")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BluePrimary, Color(0xFF0F172A))
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ลงทะเบียนพนักงาน",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                )
                Text(
                    text = "สมัครเข้าใช้งานระบบ Industrial CMMS",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("ชื่อ-นามสกุลจริง (Full Name)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Name Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_fullname_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("ชื่อบัญชีผู้ใช้ (Username)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_username_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("รหัสผ่านใหม่ (Password)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Dropdown or Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("แผนก / ตำแหน่งงาน (Role):", fontWeight = FontWeight.Bold, color = BluePrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roles.forEach { r ->
                            val isSelected = role == r
                            val thaiRole = when (r) {
                                "Manager" -> "ผู้จัดการ"
                                "Supervisor" -> "หัวหน้างาน"
                                "Technician" -> "ช่างเทคนิค"
                                else -> r
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { role = r },
                                label = { Text(thaiRole) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.register(username, password, fullName, role) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reg_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ยืนยันการลงทะเบียน", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { viewModel.navigateTo(Screen.LOGIN) },
                    modifier = Modifier.testTag("reg_back_login_button")
                ) {
                    Text("ย้อนกลับหน้าเข้าสู่ระบบ", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: CMMSViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val spareParts by viewModel.spareParts.collectAsState()

    // Calculate real-time numbers
    val totalAssets = assets.size
    val pendingTickets = tickets.count { it.status == "Pending" || it.status == "In Progress" }
    val pmPlansThisMonth = tickets.count { it.type == "PM" && it.status == "Pending" }
    
    // Calculate total expenses from spare parts linked to completed tickets
    var totalExpenses = 0.0
    tickets.filter { it.status == "Completed" && !it.partsUsedRaw.isNullOrEmpty() }.forEach { ticket ->
        ticket.partsUsedRaw?.split(";")?.filter { it.isNotEmpty() }?.forEach { partInfo ->
            val fields = partInfo.split(":")
            if (fields.size >= 3) {
                val qty = fields[1].toIntOrNull() ?: 0
                val price = fields[2].toDoubleOrNull() ?: 0.0
                totalExpenses += qty * price
            }
        }
    }

    val currencyFormat = DecimalFormat("#,##0.00")
    var showQrScannerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialGrayLight)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome and Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_factory_banner),
                contentDescription = "Smart Factory Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = "สวัสดี, ${currentUser?.fullName ?: "ผู้ใช้ระบบ"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ระดับสิทธิ์: " + when (currentUser?.role) {
                            "Admin" -> "ผู้จัดการ/ผู้ดูแลระบบ (Admin)"
                            "Manager" -> "ผู้จัดการ (Manager)"
                            "Supervisor" -> "หัวหน้างาน (Supervisor)"
                            "Technician" -> "ช่างเทคนิค (Technician)"
                            "Operator" -> "ฝ่ายผลิต (Operator)"
                            else -> "ผู้ใช้ทั่วไป (${currentUser?.role})"
                        } + if (currentUser?.isTrial == true) " [ทดลองใช้งาน 15 วัน]" else "",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }
        }

        // EXECUTIVE REAL-TIME DASHBOARD COUNTERS (M3 CARDS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "รายงานวิเคราะห์ภาพรวม Real-time",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BluePrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "เครื่องจักรทั้งหมด",
                    value = totalAssets.toString(),
                    unit = "เครื่อง",
                    icon = Icons.Default.PrecisionManufacturing,
                    color = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "งานค้างซ่อม (CM/PM)",
                    value = pendingTickets.toString(),
                    unit = "ใบแจ้ง",
                    icon = Icons.Default.Engineering,
                    color = RedCritical,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "แผนบำรุงรักษา (PM)",
                    value = pmPlansThisMonth.toString(),
                    unit = "แผนรอดำเนินงาน",
                    icon = Icons.Default.CalendarMonth,
                    color = AmberAlert,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "ค่าใช้จ่ายอะไหล่สะสม",
                    value = "฿${currencyFormat.format(totalExpenses)}",
                    unit = "",
                    icon = Icons.Default.Payments,
                    color = GreenNormal,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Scan Action Button
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showQrScannerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quick_qr_scan_button"),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code", tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("สแกน QR Code เพื่อแจ้งซ่อมด่วนทันที", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            // RECENT URGENT TICKETS LIST
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการงานซ่อมด่วน CM ที่ต้องเร่งแก้ไข",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BluePrimary
                )
                TextButton(onClick = { viewModel.navigateTo(Screen.MAINTENANCE) }) {
                    Text("ดูทั้งหมด", color = BlueSecondary, fontWeight = FontWeight.Bold)
                }
            }

            val cmTickets = tickets.filter { it.type == "CM" && it.status != "Completed" }.take(3)
            if (cmTickets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Check", tint = GreenNormal, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ไม่มีรายการแจ้งซ่อมด่วนที่ค้างอยู่", color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text("เครื่องจักรทุกตัวทำงานปกติสุขดี", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                cmTickets.forEach { ticket ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.navigateTo(Screen.MAINTENANCE) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(
                                        color = if (ticket.priority == "Critical") RedCritical else AmberAlert,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = 12f
                                    )
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Badge(containerColor = if (ticket.priority == "Critical") RedCritical else AmberAlert) {
                                        Text(
                                            text = if (ticket.priority == "Critical") "วิกฤต" else "เร่งด่วน",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = ticket.ticketNo,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ticket.title,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "เครื่องจักร: ${ticket.assetCode} (${ticket.assetName})",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = when (ticket.status) {
                                        "Pending" -> "รอดำเนินการ"
                                        "In Progress" -> "กำลังซ่อม"
                                        else -> "เสร็จสิ้น"
                                    },
                                    color = if (ticket.status == "Pending") RedCritical else AmberAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // DIALOG FOR SIMULATED QR SCANNER
        if (showQrScannerDialog) {
            SimulatedQrScannerDialog(
                assets = assets,
                onScanSuccess = { asset ->
                    showQrScannerDialog = false
                    viewModel.navigateTo(Screen.MAINTENANCE)
                    viewModel.createTicket(
                        assetId = asset.id,
                        title = "แจ้งซ่อมสแกนด่วน - ${asset.assetCode}",
                        description = "ตรวจพบบั๊กหรือความผิดปกติจากการสแกนแจ้งซ่อมหน้างานที่แผนก ${asset.location}",
                        type = "CM",
                        priority = "High"
                    )
                },
                onDismiss = { showQrScannerDialog = false }
            )
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val containerBg = when (color) {
        SleekUrgent -> SleekUrgentLight
        SleekBlue -> SleekBlueLight
        SleekWarning -> Color(0xFFFEF3C7) // soft amber container
        SleekNormal -> Color(0xFFD1FAE5) // soft emerald green container
        else -> Color.White
    }
    val contentColor = when (color) {
        SleekUrgent -> SleekUrgent
        SleekBlue -> SleekBlue
        SleekWarning -> Color(0xFFD97706) // slightly darker amber for high-contrast text
        SleekNormal -> Color(0xFF047857) // slightly darker emerald green for high-contrast text
        else -> SleekDarkText
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 4. ASSETS INVENTORY SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(viewModel: CMMSViewModel) {
    val assets by viewModel.assets.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val canCreateAsset = currentUser?.canCreateAssets() == true
    val canDeleteAsset = currentUser?.canDeleteAssets() == true

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAssetForQr by remember { mutableStateOf<Asset?>(null) }

    var assetCode by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var assetModel by remember { mutableStateOf("") }
    var assetLocation by remember { mutableStateOf("") }

    var filterText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("คลังเครื่องจักรและสินทรัพย์ (Assets)", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary),
                actions = {
                    if (canCreateAsset) {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_asset_fab")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Asset", tint = Color.White)
                        }
                    }
                }
            )
        },
        containerColor = IndustrialGrayLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search filter bar
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text("พิมพ์เพื่อค้นหาเครื่องจักรหรือสถานที่...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    focusedLabelColor = BluePrimary
                )
            )

            val filteredAssets = assets.filter {
                it.name.contains(filterText, ignoreCase = true) ||
                        it.assetCode.contains(filterText, ignoreCase = true) ||
                        it.location.contains(filterText, ignoreCase = true)
            }

            if (filteredAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = "No asset", tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ไม่พบข้อมูลเครื่องจักรในคลัง", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAssets) { asset ->
                        AssetItemCard(
                            asset = asset,
                            canDelete = canDeleteAsset,
                            onShowQr = { selectedAssetForQr = asset },
                            onDelete = { viewModel.deleteAsset(asset) }
                        )
                    }
                }
            }
        }

        // ADD ASSET DIALOG
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("เพิ่มเครื่องจักรใหม่เข้าระบบ", fontWeight = FontWeight.Bold, color = BluePrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = assetCode,
                            onValueChange = { assetCode = it },
                            label = { Text("รหัสเครื่องจักร (เช่น CNC-05)") },
                            modifier = Modifier.fillMaxWidth().testTag("asset_code_input")
                        )
                        OutlinedTextField(
                            value = assetName,
                            onValueChange = { assetName = it },
                            label = { Text("ชื่อเครื่องจักร") },
                            modifier = Modifier.fillMaxWidth().testTag("asset_name_input")
                        )
                        OutlinedTextField(
                            value = assetModel,
                            onValueChange = { assetModel = it },
                            label = { Text("ยี่ห้อ/รุ่น") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = assetLocation,
                            onValueChange = { assetLocation = it },
                            label = { Text("สถานที่ติดตั้งเครื่องจักร") },
                            modifier = Modifier.fillMaxWidth().testTag("asset_loc_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (assetCode.isNotEmpty() && assetName.isNotEmpty()) {
                                viewModel.addAsset(assetCode, assetName, assetModel, assetLocation)
                                showAddDialog = false
                                // Clear fields
                                assetCode = ""
                                assetName = ""
                                assetModel = ""
                                assetLocation = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("เพิ่มเข้าระบบ", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("ยกเลิก")
                    }
                }
            )
        }

        // QR CODE PREVIEW DIALOG
        if (selectedAssetForQr != null) {
            val asset = selectedAssetForQr!!
            Dialog(onDismissRequest = { selectedAssetForQr = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "รหัส QR สำหรับงานซ่อม",
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "พนักงานสามารถสแกนเพื่อแจ้งซ่อมเครื่องนี้ทันที",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // HIGH FIDELITY SIMULATED QR CODE VISUAL
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White)
                                .border(4.dp, BluePrimary, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Custom canvas drawing to simulate detailed high-fidelity QR Code blocks
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val size = size.width
                                val blockSize = size / 10
                                val rand = Random(asset.assetCode.hashCode().toLong())

                                // 3 Position corners
                                fun drawCornerFinder(x: Float, y: Float) {
                                    drawRect(Color.Black, Offset(x, y), androidx.compose.ui.geometry.Size(blockSize * 3, blockSize * 3))
                                    drawRect(Color.White, Offset(x + blockSize, y + blockSize), androidx.compose.ui.geometry.Size(blockSize, blockSize))
                                }

                                drawCornerFinder(0f, 0f)
                                drawCornerFinder(size - blockSize * 3, 0f)
                                drawCornerFinder(0f, size - blockSize * 3)

                                // Random data dots
                                for (i in 0 until 10) {
                                    for (j in 0 until 10) {
                                        // Skip corner finders
                                        if (i < 3 && j < 3) continue
                                        if (i >= 7 && j < 3) continue
                                        if (i < 3 && j >= 7) continue

                                        if (rand.nextBoolean()) {
                                            drawRect(
                                                color = Color.Black,
                                                topLeft = Offset(i * blockSize, j * blockSize),
                                                size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "ID: ${asset.assetCode}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BluePrimary)
                        Text(text = asset.name, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Text(text = "แผนก: ${asset.location}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { selectedAssetForQr = null },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("เสร็จสิ้น")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetItemCard(asset: Asset, canDelete: Boolean, onShowQr: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BluePrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = asset.assetCode,
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        // Badge Status
                        val statusText = when (asset.status) {
                            "Normal" -> "ปกติ (Normal)"
                            "Repairing" -> "กำลังซ่อม (Repairing)"
                            else -> "ชำรุด (Down)"
                        }
                        val statusColor = when (asset.status) {
                            "Normal" -> GreenNormal
                            "Repairing" -> AmberAlert
                            else -> RedCritical
                        }
                        Badge(containerColor = statusColor) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = asset.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "รุ่น: ${asset.model}", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = asset.location, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // QR Code Action
                IconButton(
                    onClick = onShowQr,
                    modifier = Modifier.testTag("asset_qr_btn_" + asset.assetCode)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "Generate QR", tint = BluePrimary, modifier = Modifier.size(28.dp))
                }
            }

            if (canDelete) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedCritical.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("ยืนยันการลบเครื่องจักร", color = RedCritical, fontWeight = FontWeight.Bold) },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบเครื่องจักร ${asset.assetCode} ออกจากระบบ? ข้อมูลการแจ้งซ่อมและประวัติจะสูญหาย") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCritical)
                ) {
                    Text("ลบออกจากระบบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

// 5. MAINTENANCE & JOBS SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(viewModel: CMMSViewModel) {
    val tickets by viewModel.tickets.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val spareParts by viewModel.spareParts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = CM (งานซ่อมด่วน), 1 = PM (บำรุงรักษา)
    var showCreateDialog by remember { mutableStateOf(false) }

    // Detail dialog
    var selectedTicketForDetails by remember { mutableStateOf<Ticket?>(null) }

    // Forms
    var assetId by remember { mutableStateOf(-1) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    val targetType = if (selectedTab == 0) "CM" else "PM"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("จัดการงานซ่อมบำรุง (Maintenance)", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary),
                actions = {
                    IconButton(
                        onClick = {
                            if (assets.isNotEmpty()) {
                                assetId = assets.first().id
                                showCreateDialog = true
                            }
                        },
                        modifier = Modifier.testTag("create_ticket_fab")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Ticket", tint = Color.White)
                    }
                }
            )
        },
        containerColor = IndustrialGrayLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("งานซ่อมด่วน (CM)", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    modifier = Modifier.testTag("tab_cm")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("แผนบำรุงรักษา (PM)", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    modifier = Modifier.testTag("tab_pm")
                )
            }

            val filteredTickets = tickets.filter { it.type == targetType }

            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BuildCircle, contentDescription = "No tasks", tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) "ไม่มีรายการแจ้งซ่อมด่วน CM" else "ไม่มีแผนบำรุงรักษาเชิงป้องกัน PM",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTickets) { ticket ->
                        TicketCard(
                            ticket = ticket,
                            onClick = { selectedTicketForDetails = ticket }
                        )
                    }
                }
            }
        }

        // CREATE NEW REPAIR TICKET DIALOG
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("สร้างใบแจ้งซ่อมบำรุงใหม่", fontWeight = FontWeight.Bold, color = BluePrimary) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        // Machine Selector
                        Text("เลือกเครื่องจักร / สินทรัพย์ที่ชำรุด:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expanded by remember { mutableStateOf(false) }
                        val selectedAsset = assets.find { it.id == assetId } ?: assets.firstOrNull()
                        assetId = selectedAsset?.id ?: -1

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("machine_selector_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = TextDark)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAsset?.let { "[${it.assetCode}] ${it.name}" } ?: "เลือกเครื่องจักร...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                assets.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text("[${a.assetCode}] ${a.name}") },
                                        onClick = {
                                            assetId = a.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("หัวข้อปัญหา / อาการชำรุด") },
                            modifier = Modifier.fillMaxWidth().testTag("ticket_title_input")
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("รายละเอียดปัญหาเพิ่มเติม") },
                            modifier = Modifier.fillMaxWidth().testTag("ticket_desc_input"),
                            minLines = 3
                        )

                        // Priority selection
                        Text("ระดับความรุนแรง / ความเร่งด่วน:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val priorities = listOf("Critical", "High", "Medium", "Low")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            priorities.forEach { p ->
                                val isSelected = priority == p
                                val thaiPriority = when (p) {
                                    "Critical" -> "วิกฤต"
                                    "High" -> "สูง"
                                    "Medium" -> "กลาง"
                                    "Low" -> "ต่ำ"
                                    else -> p
                                }
                                val color = when (p) {
                                    "Critical" -> RedCritical
                                    "High" -> AmberAlert
                                    "Medium" -> BlueSecondary
                                    else -> Color.Gray
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { priority = p },
                                    label = { Text(thaiPriority, color = if (isSelected) Color.White else color) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                viewModel.createTicket(
                                    assetId = assetId,
                                    title = title,
                                    description = description,
                                    type = targetType,
                                    priority = priority
                                )
                                showCreateDialog = false
                                // Clear
                                title = ""
                                description = ""
                                priority = "Medium"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("บันทึกใบแจ้งซ่อม", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("ยกเลิก")
                    }
                }
            )
        }

        // HIGH FIDELITY JOB DETAILS AND STEPS PREVIEW DIALOG
        if (selectedTicketForDetails != null) {
            // Re-fetch ticket to get live updates
            val liveTicket = tickets.find { it.id == selectedTicketForDetails!!.id } ?: selectedTicketForDetails!!
            JobDetailDialog(
                ticket = liveTicket,
                allParts = spareParts,
                viewModel = viewModel,
                onDismiss = { selectedTicketForDetails = null }
            )
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ticket.reportedDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorityColor = when (ticket.priority) {
                        "Critical" -> RedCritical
                        "High" -> AmberAlert
                        "Medium" -> BlueSecondary
                        else -> Color.Gray
                    }
                    val thaiPriority = when (ticket.priority) {
                        "Critical" -> "วิกฤต"
                        "High" -> "สูง"
                        "Medium" -> "ปกติ"
                        else -> "ต่ำ"
                    }
                    Badge(containerColor = priorityColor) {
                        Text(thaiPriority, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Text(ticket.ticketNo, fontWeight = FontWeight.Bold, color = BluePrimary, fontSize = 13.sp)
                }

                // Ticket Status Badge
                val statusColor = when (ticket.status) {
                    "Pending" -> RedCritical.copy(alpha = 0.9f)
                    "In Progress" -> AmberAlert
                    "Pending Approval" -> BlueSecondary
                    else -> GreenNormal
                }
                val statusText = when (ticket.status) {
                    "Pending" -> "รอดำเนินการ"
                    "In Progress" -> "กำลังซ่อม"
                    "Pending Approval" -> "รอตรวจและอนุมัติ"
                    else -> "ซ่อมเสร็จสิ้น"
                }
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = ticket.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "อุปกรณ์: [${ticket.assetCode}] ${ticket.assetName}", fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ผู้แจ้ง: ${ticket.reportedBy}", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
                Text(text = dateText, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// JOB DETAIL DIALOG (Saves Photo, Checks Checklist, Consumes Spare Parts, Ratings)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JobDetailDialog(
    ticket: Ticket,
    allParts: List<SparePart>,
    viewModel: CMMSViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val canManageTickets = currentUser?.canAssignOrEditTicketCore() == true
    val canApproveTicket = currentUser?.canApproveTicket() == true
    val isTechnician = currentUser?.isTechnician() == true

    var showCameraDialog by remember { mutableStateOf(false) }
    var targetCameraType by remember { mutableStateOf("Before") } // Before or After photo

    var notesInput by remember { mutableStateOf(ticket.repairNotes ?: "") }
    var satisfactionRating by remember { mutableStateOf(ticket.satisfactionRating ?: 5) }

    // State for linking parts
    var showLinkPartsDialog by remember { mutableStateOf(false) }
    var tempPartsList = remember(ticket.partsUsedRaw) {
        ticket.partsUsedRaw?.split(";")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
    }

    // PM Checklist input
    var newCheckItemName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("job_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "รายละเอียดงานซ่อม", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title and Basic Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = ticket.ticketNo, fontWeight = FontWeight.Black, fontSize = 20.sp, color = BluePrimary)
                        Text(
                            text = when (ticket.status) {
                                "Pending" -> "🔴 รอดำเนินการ"
                                "In Progress" -> "🟡 กำลังปฏิบัติงาน"
                                "Pending Approval" -> "🔵 รอตรวจและอนุมัติ"
                                else -> "🟢 เสร็จสิ้นภารกิจ"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = IndustrialGrayLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "หัวข้อ: ${ticket.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "รายละเอียด: ${ticket.description}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "เครื่องจักร: [${ticket.assetCode}] ${ticket.assetName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // BEFORE & AFTER PICTURES (INTERACTIVE CAMERA)
                    Text("การบันทึกภาพถ่ายการปฏิบัติงาน (ก่อน-หลัง)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Before Photo Box
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ภาพก่อนการแก้ไข", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray.copy(alpha = 0.5f))
                                    .clickable {
                                        if (ticket.status != "Completed" && ticket.status != "Pending Approval") {
                                            targetCameraType = "Before"
                                            showCameraDialog = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (ticket.beforePhotoUrl != null) {
                                    // Simulated Photo Rendering via custom text/icon overlay based on user selected simulation
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                        Icon(Icons.Default.Photo, contentDescription = "Photo", tint = BluePrimary, modifier = Modifier.size(24.dp))
                                        Text(ticket.beforePhotoUrl ?: "", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = BluePrimary)
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Gray)
                                        Text("กดเพื่อถ่ายรูป", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // After Photo Box
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ภาพหลังการแก้ไข", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray.copy(alpha = 0.5f))
                                    .clickable {
                                        if (ticket.status == "In Progress") {
                                            targetCameraType = "After"
                                            showCameraDialog = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (ticket.afterPhotoUrl != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                        Icon(Icons.Default.Photo, contentDescription = "Photo", tint = GreenNormal, modifier = Modifier.size(24.dp))
                                        Text(ticket.afterPhotoUrl ?: "", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = GreenNormal)
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Gray)
                                        Text("กดเพื่อถ่ายรูป", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    // PM CHECKLIST (ONLY IF PM AND HAS ITEMS)
                    if (ticket.type == "PM") {
                        Text("เช็คลิสต์แผนการบำรุงรักษาเชิงป้องกัน (PM)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                        
                        // Add checklist item form (Supervisor/Manager only)
                        if (ticket.status != "Completed" && ticket.status != "Pending Approval" && canManageTickets) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCheckItemName,
                                    onValueChange = { newCheckItemName = it },
                                    placeholder = { Text("ชื่อข้อตรวจเช็ค เช่น ตรวจกระแสไฟฟ้า") },
                                    modifier = Modifier.weight(1f).testTag("pm_item_input"),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (newCheckItemName.isNotEmpty()) {
                                            viewModel.addPmChecklistItem(ticket.id, newCheckItemName)
                                            newCheckItemName = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                ) {
                                    Text("เพิ่ม")
                                }
                            }
                        }

                        // Checklist items
                        val items = ticket.pmChecklistRaw?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()
                        if (items.isEmpty()) {
                            Text("ไม่มีเช็คลิสต์ กรุณาเพิ่มหัวข้อการตรวจเช็คด้านบน", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            items.forEachIndexed { idx, item ->
                                val parts = item.split(":")
                                val name = parts.getOrNull(0) ?: ""
                                val isDone = (parts.getOrNull(1) ?: "Pending") == "Done"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDone) GreenNormal.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            if (ticket.status != "Completed" && ticket.status != "Pending Approval") {
                                                viewModel.togglePmChecklistItem(ticket.id, idx)
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                        contentDescription = "Check state",
                                        tint = if (isDone) GreenNormal else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isDone) GreenNormal else TextDark
                                    )
                                }
                            }
                        }
                    }

                    // PARTS USED & COSTS LINKING
                    Text("การเบิกใช้อะไหล่และสรุปค่าใช้จ่ายอุปกรณ์", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                    if (ticket.status == "In Progress") {
                        Button(
                            onClick = { showLinkPartsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                            modifier = Modifier.fillMaxWidth().testTag("add_parts_used_button")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Add parts")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("เบิกอุปกรณ์ / บันทึกการใช้อะไหล่")
                        }
                    }

                    // Display used parts list
                    if (tempPartsList.isEmpty()) {
                        Text("ไม่มีข้อมูลการเบิกใช้อะไหล่สำหรับงานนี้", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        var partsTotalSum = 0.0
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            tempPartsList.forEach { partRaw ->
                                val fields = partRaw.split(":")
                                if (fields.size >= 3) {
                                    val name = fields[0]
                                    val qty = fields[1].toIntOrNull() ?: 0
                                    val cost = fields[2].toDoubleOrNull() ?: 0.0
                                    val lineTotal = qty * cost
                                    partsTotalSum += lineTotal

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$name (x$qty)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("฿${DecimalFormat("#,##0").format(lineTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("รวมค่าอะไหล่ทั้งหมด:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                Text("฿${DecimalFormat("#,##0.00").format(partsTotalSum)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GreenNormal)
                            }
                        }
                    }

                    // IF TICKET IS COMPLETED, SHOW RATINGS & NOTES
                    if (ticket.status == "Completed") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GreenNormal.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, GreenNormal.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("บันทึกการซ่อมโดยช่าง: ${ticket.assignedTo}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenNormal)
                                Text(text = ticket.repairNotes ?: "ไม่มีบันทึกเพิ่มเติม", fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("การประเมินความพึงพอใจการซ่อมบำรุง:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val rating = ticket.satisfactionRating ?: 5
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Star",
                                            tint = if (i <= rating) AmberAlert else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // IF TICKET IS PENDING APPROVAL, SHOW PROGRESS AND SUPERVISOR CONTROLS
                    if (ticket.status == "Pending Approval") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueSecondary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("สรุปรายงานอาการซ่อมโดยช่าง: ${ticket.assignedTo}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                                Text(text = ticket.repairNotes ?: "ไม่มีบันทึกเพิ่มเติม", fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

                                if (canApproveTicket) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = BlueSecondary.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("ส่วนการตรวจประเมินและอนุมัติปิดงาน (หัวหน้างาน)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = notesInput,
                                        onValueChange = { notesInput = it },
                                        label = { Text("ความเห็น / บันทึกการอนุมัติปิดงาน") },
                                        modifier = Modifier.fillMaxWidth().testTag("approval_notes_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("ประเมินคุณภาพงานซ่อม (1-5 ดาว):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(
                                                onClick = { satisfactionRating = i },
                                                modifier = Modifier.size(36.dp).testTag("approve_star_btn_$i")
                                            ) {
                                                Icon(
                                                    imageVector = if (i <= satisfactionRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                    contentDescription = "Star $i",
                                                    tint = if (i <= satisfactionRating) AmberAlert else Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("⏳ สถานะ: รอหัวหน้างานตรวจรับเครื่องจักรและประเมินคุณภาพการซ่อม", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BlueSecondary)
                                }
                            }
                        }
                    }

                    // CLOSE JOB FORM (Notes, Satisfaction, Submit)
                    if (ticket.status == "In Progress") {
                        Divider()
                        Text("สรุปการบันทึกงานซ่อมบำรุง", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("บันทึกอาการซ่อมเสร็จสิ้น / คำแนะนำเพิ่มเติม") },
                            modifier = Modifier.fillMaxWidth().testTag("notes_input"),
                            minLines = 2
                        )

                        if (!isTechnician) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ให้คะแนนความพึงพอใจงานซ่อม (1-5 ดาว):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                for (i in 1..5) {
                                    IconButton(
                                        onClick = { satisfactionRating = i },
                                        modifier = Modifier.size(36.dp).testTag("star_btn_$i")
                                    ) {
                                        Icon(
                                            imageVector = if (i <= satisfactionRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                            contentDescription = "Star $i",
                                            tint = if (i <= satisfactionRating) AmberAlert else Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM CONTROL ACTIONS
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (ticket.status == "Pending") {
                        Button(
                            onClick = {
                                viewModel.updateTicketStatus(ticket.id, "In Progress")
                            },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("start_job_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("กดรับงาน / เริ่มซ่อมบำรุง", fontWeight = FontWeight.Bold)
                        }
                    } else if (ticket.status == "In Progress") {
                        if (isTechnician) {
                            Button(
                                onClick = {
                                    val partsRaw = tempPartsList.joinToString(";")
                                    viewModel.updateTicketStatus(
                                        ticketId = ticket.id,
                                        newStatus = "Pending Approval",
                                        notes = notesInput,
                                        partsRaw = partsRaw
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("complete_job_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Submit")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ส่งรายงานซ่อม / ขออนุมัติปิดงาน", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val partsRaw = tempPartsList.joinToString(";")
                                    viewModel.updateTicketStatus(
                                        ticketId = ticket.id,
                                        newStatus = "Completed",
                                        notes = notesInput,
                                        rating = satisfactionRating,
                                        partsRaw = partsRaw
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("complete_job_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenNormal)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Done")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("เสร็จสิ้นงาน / ปิดใบแจ้งซ่อม", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (ticket.status == "Pending Approval" && canApproveTicket) {
                        Button(
                            onClick = {
                                val partsRaw = tempPartsList.joinToString(";")
                                viewModel.updateTicketStatus(
                                    ticketId = ticket.id,
                                    newStatus = "Completed",
                                    notes = notesInput,
                                    rating = satisfactionRating,
                                    partsRaw = partsRaw
                                )
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("approve_job_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNormal)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Approve")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("อนุมัติและปิดใบงานซ่อมบำรุง", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SIMULATED CAMERA DIALOG
        if (showCameraDialog) {
            SimulatedCameraDialog(
                type = targetCameraType,
                onCapture = { mockPhotoName ->
                    if (targetCameraType == "Before") {
                        viewModel.addBeforePhoto(ticket.id, mockPhotoName)
                    } else {
                        viewModel.addAfterPhoto(ticket.id, mockPhotoName)
                    }
                    showCameraDialog = false
                },
                onDismiss = { showCameraDialog = false }
            )
        }

        // DIALOG FOR PARTS SELECTION & LINKING
        if (showLinkPartsDialog) {
            var selectedPartId by remember { mutableStateOf(allParts.firstOrNull()?.id ?: -1) }
            var qtyInput by remember { mutableStateOf("1") }

            AlertDialog(
                onDismissRequest = { showLinkPartsDialog = false },
                title = { Text("เบิกใช้อะไหล่จากคลัง", fontWeight = FontWeight.Bold, color = BluePrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("เลือกรายการอะไหล่:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var partDropdownExpanded by remember { mutableStateOf(false) }
                        val activePart = allParts.find { it.id == selectedPartId } ?: allParts.firstOrNull()

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { partDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("spare_part_dropdown"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = TextDark)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activePart?.let { "${it.partCode} - ${it.name} (คลังคงเหลือ: ${it.stockQuantity})" } ?: "เลือกอะไหล่...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }
                            DropdownMenu(expanded = partDropdownExpanded, onDismissRequest = { partDropdownExpanded = false }) {
                                allParts.forEach { part ->
                                    DropdownMenuItem(
                                        text = { Text("${part.partCode} - ${part.name} (เหลือ: ${part.stockQuantity})") },
                                        onClick = {
                                            selectedPartId = part.id
                                            partDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it },
                            label = { Text("ระบุจำนวนการเบิกใช้") },
                            modifier = Modifier.fillMaxWidth().testTag("part_qty_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val activePart = allParts.find { it.id == selectedPartId }
                            val qty = qtyInput.toIntOrNull() ?: 1
                            if (activePart != null && qty > 0) {
                                // Add to temporary local parts list format
                                tempPartsList.add("${activePart.name}:$qty:${activePart.unitPrice}")
                                showLinkPartsDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("บันทึกการเบิกอะไหล่", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLinkPartsDialog = false }) {
                        Text("ยกเลิก")
                    }
                }
            )
        }
    }
}

// SIMULATED INTERACTIVE QR CODE SCANNER (Fully custom visual feedback!)
@Composable
fun SimulatedQrScannerDialog(
    assets: List<Asset>,
    onScanSuccess: (Asset) -> Unit,
    onDismiss: () -> Unit
) {
    // Laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("สแกนเนอร์บาร์โค้ด / QR Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("จำลองระบบสแกนกล้องหน้างานจริง", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 20.dp))

                // Outer scan frame
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .border(3.dp, BlueSecondary, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated camera view screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        // Drawing corner markers in camera viewfinder
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val markerLen = 30f
                            val strokeW = 8f

                            // Top Left
                            drawLine(Color.Green, Offset(0f, 0f), Offset(markerLen, 0f), strokeW)
                            drawLine(Color.Green, Offset(0f, 0f), Offset(0f, markerLen), strokeW)

                            // Top Right
                            drawLine(Color.Green, Offset(w, 0f), Offset(w - markerLen, 0f), strokeW)
                            drawLine(Color.Green, Offset(w, 0f), Offset(w, markerLen), strokeW)

                            // Bottom Left
                            drawLine(Color.Green, Offset(0f, h), Offset(markerLen, h), strokeW)
                            drawLine(Color.Green, Offset(0f, h), Offset(0f, h - markerLen), strokeW)

                            // Bottom Right
                            drawLine(Color.Green, Offset(w, h), Offset(w - markerLen, h), strokeW)
                            drawLine(Color.Green, Offset(w, h), Offset(w, h - markerLen), strokeW)

                            // Horizontal scanner laser line
                            drawLine(
                                color = RedCritical,
                                start = Offset(0f, laserOffsetY / 200f * h),
                                end = Offset(w, laserOffsetY / 200f * h),
                                strokeWidth = 5f
                            )
                        }

                        // Simulated QR inside screen
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "Simulated QR",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("คลิกเลือกเครื่องเพื่อจำลองการแกนประสพความสำเร็จ:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                // List of assets to select as scan simulation
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(assets) { asset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScanSuccess(asset) },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("[${asset.assetCode}] ${asset.name}", color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.QrCode, contentDescription = "Scan target", tint = Color.Green, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("ยกเลิก", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// SIMULATED CAMERA VIEW (Allows selecting a mock visual scene, click shutter, returns photo string)
@Composable
fun SimulatedCameraDialog(
    type: String,
    onCapture: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val beforeScenes = listOf("หน้าปัดควบคุมชำรุด.png", "น้ำมันไฮดรอลิครั่วซึม.png", "ตลับลูกปืนแตกเสียงดัง.png", "สายพานขาดชำรุด.png")
    val afterScenes = listOf("เปลี่ยนแผงปุ่มกดเรียบร้อย.png", "ซีลน้ำมันไฮดรอลิคใหม่.png", "ตลับลูกปืนเปลี่ยนสำเร็จ.png", "ประกอบสายพานใหม่เสร็จสิ้น.png")
    val targetScenes = if (type == "Before") beforeScenes else afterScenes

    var selectedSceneIndex by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("กล้องจำลองการปฏิบัติงาน", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ถ่ายภาพเพื่อแนบประกอบใบแจ้งซ่อม (${if (type == "Before") "ก่อนซ่อม" else "หลังซ่อม"})", color = Color.Gray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder gridlines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // vertical grids
                        drawLine(Color.White.copy(alpha = 0.2f), Offset(w / 3f, 0f), Offset(w / 3f, h), 1f)
                        drawLine(Color.White.copy(alpha = 0.2f), Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), 1f)

                        // horizontal grids
                        drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, h / 3f), Offset(w, h / 3f), 1f)
                        drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), 1f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cam", tint = BlueSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = targetScenes[selectedSceneIndex],
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("เลือกภาพสถานการณ์จำลอง:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                // Scene selectors
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    targetScenes.forEachIndexed { index, scene ->
                        val isSelected = selectedSceneIndex == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSceneIndex = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BlueSecondary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                            ),
                            border = if (isSelected) BorderStroke(1.dp, BlueSecondary) else null
                        ) {
                            Text(
                                text = scene,
                                color = if (isSelected) Color.White else Color.LightGray,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Capture Shutter Button
                IconButton(
                    onClick = {
                        onCapture(targetScenes[selectedSceneIndex])
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("ยกเลิก", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. SPARE PARTS INVENTORY SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparePartsScreen(viewModel: CMMSViewModel) {
    val parts by viewModel.spareParts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val canCreateParts = currentUser?.canCreateSpareParts() == true
    val canEditStock = currentUser?.canEditSparePartsStock() == true

    var showAddPartDialog by remember { mutableStateOf(false) }

    var pCode by remember { mutableStateOf("") }
    var pName by remember { mutableStateOf("") }
    var pStock by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pUnit by remember { mutableStateOf("ชิ้น") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("คลังอะไหล่และอุปกรณ์ (Spare Parts)", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary),
                actions = {
                    if (canCreateParts) {
                        IconButton(
                            onClick = { showAddPartDialog = true },
                            modifier = Modifier.testTag("add_part_fab")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Part", tint = Color.White)
                        }
                    }
                }
            )
        },
        containerColor = IndustrialGrayLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "รายงานสต็อกอะไหล่เครื่องจักร",
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (parts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Category, contentDescription = "No parts", tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ไม่มีข้อมูลอะไหล่ในระบบ", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(parts) { part ->
                        SparePartCard(
                            part = part,
                            canEditStock = canEditStock,
                            onStockChange = { diff ->
                                viewModel.updatePartStock(part.id, diff)
                            }
                        )
                    }
                }
            }
        }

        // ADD SPARE PART DIALOG
        if (showAddPartDialog) {
            AlertDialog(
                onDismissRequest = { showAddPartDialog = false },
                title = { Text("บันทึกอะไหล่ใหม่เข้าสต็อก", fontWeight = FontWeight.Bold, color = BluePrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pCode,
                            onValueChange = { pCode = it },
                            label = { Text("รหัสอะไหล่ (เช่น SP-005)") },
                            modifier = Modifier.fillMaxWidth().testTag("part_code_input")
                        )
                        OutlinedTextField(
                            value = pName,
                            onValueChange = { pName = it },
                            label = { Text("ชื่อรายการอะไหล่") },
                            modifier = Modifier.fillMaxWidth().testTag("part_name_input")
                        )
                        OutlinedTextField(
                            value = pStock,
                            onValueChange = { pStock = it },
                            label = { Text("จำนวนเริ่มต้นเข้าคลัง") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("part_stock_input")
                        )
                        OutlinedTextField(
                            value = pPrice,
                            onValueChange = { pPrice = it },
                            label = { Text("ราคาต่อหน่วย (บาท)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("part_price_input")
                        )
                        OutlinedTextField(
                            value = pUnit,
                            onValueChange = { pUnit = it },
                            label = { Text("หน่วยนับ (เช่น ชิ้น, ตัว, เส้น)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val stockVal = pStock.toIntOrNull() ?: 0
                            val priceVal = pPrice.toDoubleOrNull() ?: 0.0
                            if (pCode.isNotEmpty() && pName.isNotEmpty()) {
                                viewModel.addSparePart(pCode, pName, stockVal, priceVal, pUnit)
                                showAddPartDialog = false
                                // Clear
                                pCode = ""
                                pName = ""
                                pStock = ""
                                pPrice = ""
                                pUnit = "ชิ้น"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("บันทึกเข้าระบบ", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPartDialog = false }) {
                        Text("ยกเลิก")
                    }
                }
            )
        }
    }
}

@Composable
fun SparePartCard(part: SparePart, canEditStock: Boolean, onStockChange: (Int) -> Unit) {
    val isLowStock = part.stockQuantity <= 10

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BluePrimary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = part.partCode,
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isLowStock) {
                            Badge(containerColor = RedCritical) {
                                Text(
                                    "สต็อกใกล้หมด!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = part.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "ราคาต่อหน่วย: ฿${DecimalFormat("#,##0.00").format(part.unitPrice)} / ${part.unit}", fontSize = 12.sp, color = Color.Gray)
                }

                // Live stock highlight count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${part.stockQuantity}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLowStock) RedCritical else BluePrimary
                    )
                    Text(text = part.unit, fontSize = 11.sp, color = Color.LightGray)
                }
            }

            if (canEditStock) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // RESTOCK / STEPS BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ปรับปรุงจำนวนคลัง:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledIconButton(
                            onClick = { onStockChange(-5) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = RedCritical.copy(alpha = 0.15f), contentColor = RedCritical),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-5", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        FilledIconButton(
                            onClick = { onStockChange(-1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = TextDark),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Deduct 1", modifier = Modifier.size(14.dp))
                        }
                        FilledIconButton(
                            onClick = { onStockChange(1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = TextDark),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add 1", modifier = Modifier.size(14.dp))
                        }
                        FilledIconButton(
                            onClick = { onStockChange(10) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreenNormal.copy(alpha = 0.15f), contentColor = GreenNormal),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+10", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// 7. NOTIFICATION SETTINGS SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: CMMSViewModel) {
    val config by viewModel.notificationConfig.collectAsState()

    var tokenInput by remember(config) { mutableStateOf(config.lineNotifyToken) }
    var emailInput by remember(config) { mutableStateOf(config.emailRecipient) }
    var popIpInput by remember(config) { mutableStateOf(config.popLiteIp) }

    var lineEnabled by remember(config) { mutableStateOf(config.isLineEnabled) }
    var emailEnabled by remember(config) { mutableStateOf(config.isEmailEnabled) }
    var popEnabled by remember(config) { mutableStateOf(config.isPopLiteEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ตั้งค่าเครือข่ายและการแจ้งเตือน", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        containerColor = IndustrialGrayLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ช่องทางแจ้งเตือนทีมช่างบำรุงรักษา (Notifications)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BluePrimary
                    )
                    Text(
                        text = "เมื่อพนักงานคุมเครื่องจักร สแกนแจ้งซ่อม ระบบจะส่งสัญญาณพุชเตือนทันทีทางช่องทางต่อไปนี้:",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // 1. LINE Notify Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "LINE", tint = Color(0xFF06C755))
                            Column {
                                Text("LINE Notify", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ส่งพุชแจ้งเตือนทางกลุ่มไลน์ช่าง", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                        Switch(
                            checked = lineEnabled,
                            onCheckedChange = { lineEnabled = it },
                            modifier = Modifier.testTag("line_toggle")
                        )
                    }

                    if (lineEnabled) {
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            label = { Text("LINE Notify Access Token") },
                            placeholder = { Text("กรอก Token ไลน์สำหรับกลุ่มแจ้งซ่อม") },
                            modifier = Modifier.fillMaxWidth().testTag("line_token_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. E-mail Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = BlueSecondary)
                            Column {
                                Text("อีเมลผู้รับผิดชอบ (E-mail)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ส่งรายงานอาการชำรุดทางอีเมลหลัก", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                        Switch(
                            checked = emailEnabled,
                            onCheckedChange = { emailEnabled = it },
                            modifier = Modifier.testTag("email_toggle")
                        )
                    }

                    if (emailEnabled) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("ที่อยู่อีเมลผู้จัดการโรงงาน / หัวหน้าช่าง") },
                            placeholder = { Text("เช่น head.tech@factory.com") },
                            modifier = Modifier.fillMaxWidth().testTag("email_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Pop Lite Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.ConnectedTv, contentDescription = "Pop Lite", tint = AmberAlert)
                            Column {
                                Text("ระบบ Pop Lite IoT Notification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ส่งรหัสสัญญาณตรงจอแผนกบำรุงรักษา", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                        Switch(
                            checked = popEnabled,
                            onCheckedChange = { popEnabled = it },
                            modifier = Modifier.testTag("pop_lite_toggle")
                        )
                    }

                    if (popEnabled) {
                        OutlinedTextField(
                            value = popIpInput,
                            onValueChange = { popIpInput = it },
                            label = { Text("ที่อยู่ IP ของบอร์ดรับสัญญาณ Pop Lite") },
                            placeholder = { Text("เช่น 192.168.1.150") },
                            modifier = Modifier.fillMaxWidth().testTag("pop_ip_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Save Settings button
            Button(
                onClick = {
                    viewModel.saveNotificationConfig(
                        lineToken = tokenInput,
                        email = emailInput,
                        popIp = popIpInput,
                        lineOn = lineEnabled,
                        emailOn = emailEnabled,
                        popOn = popEnabled
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("บันทึกการตั้งค่าเครือข่าย", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
