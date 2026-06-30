package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val userDao: UserDao,
    private val assetDao: AssetDao,
    private val ticketDao: TicketDao,
    private val sparePartDao: SparePartDao,
    private val notificationDao: NotificationConfigDao
) {
    // Users
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    suspend fun login(username: String, password: String): User? = userDao.login(username, password)
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    suspend fun insertUser(user: User) = userDao.insertUser(user)

    // Assets
    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    suspend fun getAssetById(id: Int): Asset? = assetDao.getAssetById(id)
    suspend fun getAssetByCode(code: String): Asset? = assetDao.getAssetByCode(code)
    suspend fun insertAsset(asset: Asset) = assetDao.insertAsset(asset)
    suspend fun updateAsset(asset: Asset) = assetDao.updateAsset(asset)
    suspend fun deleteAsset(asset: Asset) = assetDao.deleteAsset(asset)

    // Tickets
    val allTickets: Flow<List<Ticket>> = ticketDao.getAllTickets()
    suspend fun getTicketById(id: Int): Ticket? = ticketDao.getTicketById(id)
    suspend fun insertTicket(ticket: Ticket) = ticketDao.insertTicket(ticket)
    suspend fun updateTicket(ticket: Ticket) = ticketDao.updateTicket(ticket)
    suspend fun deleteTicket(ticket: Ticket) = ticketDao.deleteTicket(ticket)
    fun getTicketsCountByStatus(status: String): Flow<Int> = ticketDao.getTicketsCountByStatus(status)

    // Spare Parts
    val allSpareParts: Flow<List<SparePart>> = sparePartDao.getAllSpareParts()
    suspend fun insertSparePart(part: SparePart) = sparePartDao.insertSparePart(part)
    suspend fun updateSparePart(part: SparePart) = sparePartDao.updateSparePart(part)
    suspend fun deleteSparePart(part: SparePart) = sparePartDao.deleteSparePart(part)

    // Notification Config
    val configFlow: Flow<NotificationConfig?> = notificationDao.getConfigFlow()
    suspend fun getConfig(): NotificationConfig? = notificationDao.getConfig()
    suspend fun saveConfig(config: NotificationConfig) = notificationDao.saveConfig(config)

    // Prepopulate data if database is empty
    suspend fun prepopulateDataIfNeeded() {
        val users = userDao.getAllUsers().firstOrNull()
        if (users.isNullOrEmpty()) {
            // 1. Insert default admin users
            userDao.insertUser(User(username = "admin", password = "PTP@min_2026", fullName = "ผู้ดูแลระบบสูงสุด (System Admin)", role = "Admin"))
            userDao.insertUser(User(username = "niwat.T", password = "123456", fullName = "คุณนิวัฒน์ ทองแท้ (Factory Manager)", role = "Admin"))
            userDao.insertUser(User(username = "somchai.m", password = "123456", fullName = "สมชาย มีดี (Technician Lead)", role = "Technician"))
            userDao.insertUser(User(username = "operator1", password = "123456", fullName = "วิชัย รักดี (Operator Line A)", role = "Operator"))

            // 2. Insert default assets
            assetDao.insertAsset(Asset(
                assetCode = "MC-001",
                name = "เครื่องจักรกลซีเอ็นซี 5 แกน (CNC 5-Axis Milling Machine)",
                model = "DMG MORI DMU 50",
                location = "Production Area A",
                status = "Normal",
                purchaseDate = "15/03/2024",
                qrCodeData = "CMMS-MC-001"
            ))
            assetDao.insertAsset(Asset(
                assetCode = "MC-002",
                name = "เครื่องปั๊มลมอุตสาหกรรม (Rotary Screw Air Compressor)",
                model = "Sullair LS-16",
                location = "Utility Room 1",
                status = "Repairing",
                purchaseDate = "22/08/2023",
                qrCodeData = "CMMS-MC-002"
            ))
            assetDao.insertAsset(Asset(
                assetCode = "MC-003",
                name = "หม้อกำเนิดไอน้ำอุตสาหกรรม (Water Tube Steam Boiler)",
                model = "Miura LX-200",
                location = "Boiler House",
                status = "Down",
                purchaseDate = "10/01/2022",
                qrCodeData = "CMMS-MC-003"
            ))
            assetDao.insertAsset(Asset(
                assetCode = "MC-004",
                name = "เครื่องฉีดขึ้นรูปพลาสติก (Plastic Injection Molding Machine)",
                model = "Sumitomo SE-EV",
                location = "Production Area B",
                status = "Normal",
                purchaseDate = "05/11/2024",
                qrCodeData = "CMMS-MC-004"
            ))

            // 3. Insert default spare parts
            sparePartDao.insertSparePart(SparePart(
                partCode = "SP-001",
                name = "ตลับลูกปืนเม็ดกลม (Deep Groove Ball Bearing 6204-ZZ)",
                stockQuantity = 120,
                unitPrice = 150.0,
                unit = "ชิ้น"
            ))
            sparePartDao.insertSparePart(SparePart(
                partCode = "SP-002",
                name = "น้ำมันไฮดรอลิคอุตสาหกรรม (Shell Tellus S2 M46)",
                stockQuantity = 15,
                unitPrice = 3200.0,
                unit = "ถัง"
            ))
            sparePartDao.insertSparePart(SparePart(
                partCode = "SP-003",
                name = "สายพานร่องวีส่งกำลัง (V-Belt Gates Tri-Power A48)",
                stockQuantity = 45,
                unitPrice = 280.0,
                unit = "เส้น"
            ))
            sparePartDao.insertSparePart(SparePart(
                partCode = "SP-004",
                name = "ไส้กรองอากาศอัดห้องปั๊มลม (Main Line Air Filter Element)",
                stockQuantity = 8,
                unitPrice = 1200.0,
                unit = "ชิ้น"
            ))

            // 4. Insert default tickets
            ticketDao.insertTicket(Ticket(
                ticketNo = "CM-2026-0001",
                assetId = 2,
                assetName = "เครื่องปั๊มลมอุตสาหกรรม (Rotary Screw Air Compressor)",
                assetCode = "MC-002",
                title = "ความดันลมตกผิดปกติในระบบหลัก",
                description = "ตรวจพบเกจวัดความดันแสดงผลต่ำกว่า 4 บาร์ ส่งผลให้เครื่องมือลมในไลน์ผลิตทำงานได้ไม่เต็มประสิทธิภาพ",
                type = "CM",
                priority = "High",
                status = "In Progress",
                reportedBy = "วิชัย รักดี",
                assignedTo = "สมชาย มีดี"
            ))
            ticketDao.insertTicket(Ticket(
                ticketNo = "PM-2026-0002",
                assetId = 1,
                assetName = "เครื่องจักรกลซีเอ็นซี 5 แกน (CNC 5-Axis Milling Machine)",
                assetCode = "MC-001",
                title = "แผนบำรุงรักษาเชิงป้องกันประจำเดือน (PM Level 1)",
                description = "ตรวจสอบระดับน้ำมันหล่อลื่น ทำความสะอาดระบบกรองฝุ่นเหล็ก และตรวจสอบความตึงสายพานแกนขับเคลื่อน",
                type = "PM",
                priority = "Medium",
                status = "Pending",
                reportedBy = "ระบบอัตโนมัติ (PM Schedule)",
                pmChecklistRaw = "ตรวจเช็คระดับน้ำมันหล่อลื่น:Done;ทำความสะอาดตะแกรงกรอง:Done;ตรวจสอบระดับไฟสถานะ:Pending;ทดสอบความเที่ยงตรงแกนร่วม:Pending"
            ))
            ticketDao.insertTicket(Ticket(
                ticketNo = "CM-2026-0003",
                assetId = 3,
                assetName = "หม้อกำเนิดไอน้ำอุตสาหกรรม (Water Tube Steam Boiler)",
                assetCode = "MC-003",
                title = "หัวเผาดับกระทันหันและมีสัญญาณเตือนลมขัดข้อง",
                description = "ไฟสัญญานขึ้นโค้ด Error E-04 หัวเผาไม่สามารถจุดระเบิดได้ ตรวจสอบเบื้องต้นไม่มีเปลวไฟ",
                type = "CM",
                priority = "Critical",
                status = "Pending",
                reportedBy = "ระบบเซ็นเซอร์หลัก"
            ))

            // 5. Default Notification Config
            notificationDao.saveConfig(NotificationConfig(
                id = 1,
                lineNotifyToken = "LineNotify_Demo_Token_2026",
                emailRecipient = "niwat.t@factory-ptp.com",
                popLiteIp = "192.168.1.200",
                isLineEnabled = true,
                isEmailEnabled = false,
                isPopLiteEnabled = true
            ))
        }
    }
}
