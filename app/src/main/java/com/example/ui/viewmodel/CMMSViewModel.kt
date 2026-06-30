package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN, REGISTER, DASHBOARD, ASSETS, MAINTENANCE, SPARE_PARTS, SETTINGS
}

class CMMSViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        database.userDao(),
        database.assetDao(),
        database.ticketDao(),
        database.sparePartDao(),
        database.notificationConfigDao()
    )

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    // Database Flows
    val assets: StateFlow<List<Asset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tickets: StateFlow<List<Ticket>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spareParts: StateFlow<List<SparePart>> = repository.allSpareParts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationConfig: StateFlow<NotificationConfig> = repository.configFlow
        .map { it ?: NotificationConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationConfig())

    // UI Toast/Notification Messages
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateDataIfNeeded()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // AUTH ACTIONS
    fun login(usernameInput: String, passwordInput: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.login(usernameInput, passwordInput)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = Screen.DASHBOARD
                emitEvent("เข้าสู่ระบบสำเร็จ: ยินดีต้อนรับ ${user.fullName}")
            } else {
                _loginError.value = "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"
                emitEvent("เข้าสู่ระบบล้มเหลว")
            }
        }
    }

    fun register(usernameInput: String, passwordInput: String, fullNameInput: String, roleInput: String) {
        viewModelScope.launch {
            val existing = repository.getUserByUsername(usernameInput)
            if (existing != null) {
                _loginError.value = "ชื่อผู้ใช้นี้ถูกใช้งานแล้ว"
                emitEvent("ชื่อผู้ใช้ซ้ำ")
                return@launch
            }
            val newUser = User(
                username = usernameInput,
                password = passwordInput,
                fullName = fullNameInput,
                role = roleInput
            )
            repository.insertUser(newUser)
            _registerSuccess.value = true
            emitEvent("ลงทะเบียนสำเร็จ กรุณาเข้าสู่ระบบ")
            _currentScreen.value = Screen.LOGIN
        }
    }

    fun startFreeTrial() {
        viewModelScope.launch {
            val trialUser = User(
                username = "trial_user_${System.currentTimeMillis().toString().takeLast(4)}",
                password = "trial",
                fullName = "ผู้ทดลองใช้งาน (15-Day Free Trial)",
                role = "Admin",
                isTrial = true,
                trialStartDate = System.currentTimeMillis()
            )
            repository.insertUser(trialUser)
            _currentUser.value = trialUser
            _currentScreen.value = Screen.DASHBOARD
            emitEvent("เริ่มต้นสิทธิ์ทดลองใช้งานฟรี 15 วันสำเร็จ!")
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.LOGIN
        viewModelScope.launch {
            emitEvent("ออกจากระบบแล้ว")
        }
    }

    // ASSET ACTIONS
    fun addAsset(code: String, name: String, model: String, location: String) {
        viewModelScope.launch {
            val existing = repository.getAssetByCode(code)
            if (existing != null) {
                emitEvent("รหัสเครื่องจักรนี้มีอยู่แล้วในระบบ")
                return@launch
            }
            val newAsset = Asset(
                assetCode = code,
                name = name,
                model = model,
                location = location,
                status = "Normal",
                purchaseDate = "29/06/2026",
                qrCodeData = "CMMS-$code"
            )
            repository.insertAsset(newAsset)
            emitEvent("เพิ่มเครื่องจักร $code สำเร็จ")
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
            emitEvent("ลบเครื่องจักร ${asset.assetCode} แล้ว")
        }
    }

    fun updateAssetStatus(assetId: Int, newStatus: String) {
        viewModelScope.launch {
            val asset = repository.getAssetById(assetId)
            if (asset != null) {
                val updated = asset.copy(status = newStatus)
                repository.updateAsset(updated)
            }
        }
    }

    // TICKET ACTIONS
    fun createTicket(assetId: Int, title: String, description: String, type: String, priority: String) {
        viewModelScope.launch {
            val asset = repository.getAssetById(assetId) ?: return@launch
            val count = tickets.value.size + 1
            val prefix = if (type == "CM") "CM" else "PM"
            val ticketNo = "$prefix-2026-${String.format("%04d", count)}"
            
            val newTicket = Ticket(
                ticketNo = ticketNo,
                assetId = asset.id,
                assetName = asset.name,
                assetCode = asset.assetCode,
                title = title,
                description = description,
                type = type,
                priority = priority,
                status = "Pending",
                reportedBy = _currentUser.value?.fullName ?: "ผู้แจ้งไม่ระบุชื่อ"
            )
            repository.insertTicket(newTicket)
            
            // Auto update asset status to Repairing or Down depending on ticket priority if type is CM
            if (type == "CM") {
                val newAssetStatus = if (priority == "Critical") "Down" else "Repairing"
                updateAssetStatus(asset.id, newAssetStatus)
            }

            emitEvent("แจ้งซ่อมสำเร็จ หมายเลขใบแจ้งซ่อม: $ticketNo")
            triggerSystemNotification(newTicket)
        }
    }

    fun updateTicketStatus(ticketId: Int, newStatus: String, notes: String = "", rating: Int? = null, partsRaw: String? = null) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId) ?: return@launch
            var updated = ticket.copy(status = newStatus)
            
            if (newStatus == "Completed") {
                updated = updated.copy(
                    completedDate = System.currentTimeMillis(),
                    satisfactionRating = rating,
                    repairNotes = notes,
                    partsUsedRaw = partsRaw
                )
                // When complete, update the machine status back to normal
                updateAssetStatus(ticket.assetId, "Normal")
                
                // Deduct spare parts inventory
                deductPartsInventory(partsRaw)
            } else if (newStatus == "Pending Approval") {
                updated = updated.copy(
                    repairNotes = notes,
                    partsUsedRaw = partsRaw
                )
            } else if (newStatus == "In Progress") {
                updated = updated.copy(
                    assignedTo = _currentUser.value?.fullName ?: "ช่างซ่อมประจำโครงการ"
                )
            }
            
            repository.updateTicket(updated)
            emitEvent("อัปเดตใบแจ้งซ่อม ${ticket.ticketNo} เป็น $newStatus")
        }
    }

    fun addPmChecklistItem(ticketId: Int, item: String) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId) ?: return@launch
            val list = ticket.pmChecklistRaw?.split(";")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            list.add("$item:Pending")
            val updated = ticket.copy(pmChecklistRaw = list.joinToString(";"))
            repository.updateTicket(updated)
        }
    }

    fun togglePmChecklistItem(ticketId: Int, index: Int) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId) ?: return@launch
            val list = ticket.pmChecklistRaw?.split(";")?.filter { it.isNotEmpty() }?.toMutableList() ?: return@launch
            if (index in list.indices) {
                val parts = list[index].split(":")
                val name = parts.getOrNull(0) ?: ""
                val status = parts.getOrNull(1) ?: "Pending"
                val newStatus = if (status == "Done") "Pending" else "Done"
                list[index] = "$name:$newStatus"
                val updated = ticket.copy(pmChecklistRaw = list.joinToString(";"))
                repository.updateTicket(updated)
            }
        }
    }

    fun addBeforePhoto(ticketId: Int, url: String) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId) ?: return@launch
            val updated = ticket.copy(beforePhotoUrl = url)
            repository.updateTicket(updated)
            emitEvent("บันทึกรูปภาพก่อนซ่อมแล้ว")
        }
    }

    fun addAfterPhoto(ticketId: Int, url: String) {
        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId) ?: return@launch
            val updated = ticket.copy(afterPhotoUrl = url)
            repository.updateTicket(updated)
            emitEvent("บันทึกรูปภาพหลังซ่อมแล้ว")
        }
    }

    // SPARE PARTS ACTIONS
    fun addSparePart(code: String, name: String, stock: Int, price: Double, unit: String) {
        viewModelScope.launch {
            val newPart = SparePart(
                partCode = code,
                name = name,
                stockQuantity = stock,
                unitPrice = price,
                unit = unit
            )
            repository.insertSparePart(newPart)
            emitEvent("เพิ่มอะไหล่ $code สำเร็จ")
        }
    }

    fun updatePartStock(partId: Int, change: Int) {
        viewModelScope.launch {
            // Locate part
            val part = spareParts.value.find { it.id == partId } ?: return@launch
            val updated = part.copy(stockQuantity = (part.stockQuantity + change).coerceAtLeast(0))
            repository.updateSparePart(updated)
            emitEvent("ปรับปรุงสต็อกของ ${part.partCode} แล้ว")
        }
    }

    private suspend fun deductPartsInventory(partsRaw: String?) {
        if (partsRaw.isNullOrEmpty()) return
        // Format: Name:Qty:Cost;Name2:Qty2:Cost2
        val items = partsRaw.split(";").filter { it.isNotEmpty() }
        for (item in items) {
            val parts = item.split(":")
            if (parts.size >= 2) {
                val name = parts[0]
                val qty = parts[1].toIntOrNull() ?: 0
                val dbPart = spareParts.value.find { it.name == name }
                if (dbPart != null) {
                    val updated = dbPart.copy(stockQuantity = (dbPart.stockQuantity - qty).coerceAtLeast(0))
                    repository.updateSparePart(updated)
                }
            }
        }
    }

    // NOTIFICATION SETTINGS ACTIONS
    fun saveNotificationConfig(
        lineToken: String,
        email: String,
        popIp: String,
        lineOn: Boolean,
        emailOn: Boolean,
        popOn: Boolean
    ) {
        viewModelScope.launch {
            val config = NotificationConfig(
                id = 1,
                lineNotifyToken = lineToken,
                emailRecipient = email,
                popLiteIp = popIp,
                isLineEnabled = lineOn,
                isEmailEnabled = emailOn,
                isPopLiteEnabled = popOn
            )
            repository.saveConfig(config)
            emitEvent("บันทึกการตั้งค่าระบบแจ้งเตือนสำเร็จ")
        }
    }

    // HELPER EVENTS
    private suspend fun emitEvent(message: String) {
        _uiEvent.emit(message)
    }

    private fun triggerSystemNotification(ticket: Ticket) {
        viewModelScope.launch {
            val config = notificationConfig.value
            val channelDetails = mutableListOf<String>()
            if (config.isLineEnabled && config.lineNotifyToken.isNotEmpty()) {
                channelDetails.add("LINE Notify [Token: ${config.lineNotifyToken.take(6)}...]")
            }
            if (config.isEmailEnabled && config.emailRecipient.isNotEmpty()) {
                channelDetails.add("E-mail [${config.emailRecipient}]")
            }
            if (config.isPopLiteEnabled && config.popLiteIp.isNotEmpty()) {
                channelDetails.add("Pop Lite Server [IP: ${config.popLiteIp}]")
            }

            if (channelDetails.isNotEmpty()) {
                emitEvent("🔔 ส่งข้อความแจ้งเตือนช่างทันทีผ่าน: " + channelDetails.joinToString(", "))
            }
        }
    }
}
