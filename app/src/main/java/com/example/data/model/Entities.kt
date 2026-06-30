package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: String, // Admin, Technician, Operator
    val isTrial: Boolean = false,
    val trialStartDate: Long = 0L
) {
    fun isManagerOrAdmin(): Boolean {
        return role.equals("Admin", ignoreCase = true) || role.equals("Manager", ignoreCase = true) || username == "admin" || username == "niwat.T" || username == "niwat"
    }

    fun isSupervisor(): Boolean {
        return role.equals("Supervisor", ignoreCase = true)
    }

    fun isTechnician(): Boolean {
        return role.equals("Technician", ignoreCase = true)
    }

    // 1. Assets Permissions
    fun canCreateAssets(): Boolean = isManagerOrAdmin() || isSupervisor()
    fun canEditAssets(): Boolean = isManagerOrAdmin() || isSupervisor()
    fun canDeleteAssets(): Boolean = isManagerOrAdmin()

    // 2. Spare Parts Permissions
    fun canCreateSpareParts(): Boolean = isManagerOrAdmin() || isSupervisor()
    fun canEditSparePartsStock(): Boolean = isManagerOrAdmin() || isSupervisor()
    fun canDeleteSpareParts(): Boolean = isManagerOrAdmin()

    // 3. Maintenance Tasks Permissions
    fun canCreateTicket(): Boolean = true
    fun canAssignOrEditTicketCore(): Boolean = isManagerOrAdmin() || isSupervisor()
    fun canApproveTicket(): Boolean = isManagerOrAdmin() || isSupervisor()
}

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetCode: String,
    val name: String,
    val model: String,
    val location: String,
    val status: String, // Normal (ปกติ), Repairing (กำลังซ่อม), Down (ชำรุด/หยุดทำงาน)
    val purchaseDate: String,
    val qrCodeData: String
)

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketNo: String,
    val assetId: Int,
    val assetName: String,
    val assetCode: String,
    val title: String,
    val description: String,
    val type: String, // CM (ซ่อมด่วน), PM (บำรุงรักษาเชิงป้องกัน)
    val priority: String, // Critical (วิกฤต), High (สูง), Medium (ปานกลาง), Low (ต่ำ)
    val status: String, // Pending (รอดำเนินการ), In Progress (กำลังดำเนินงาน), Completed (เสร็จสิ้น)
    val reportedBy: String,
    val reportedDate: Long = System.currentTimeMillis(),
    val assignedTo: String = "", // ชื่อช่างที่รับผิดชอบ
    val beforePhotoUrl: String? = null,
    val afterPhotoUrl: String? = null,
    val completedDate: Long? = null,
    val partsUsedRaw: String? = null, // Format: Name:Qty:Cost;Name:Qty:Cost
    val satisfactionRating: Int? = null, // 1 - 5 ดาว
    val repairNotes: String? = null,
    val pmChecklistRaw: String? = null // Format: ItemName1:Done;ItemName2:Done
)

@Entity(tableName = "spare_parts")
data class SparePart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partCode: String,
    val name: String,
    val stockQuantity: Int,
    val unitPrice: Double,
    val unit: String
)

@Entity(tableName = "notification_config")
data class NotificationConfig(
    @PrimaryKey val id: Int = 1,
    val lineNotifyToken: String = "",
    val emailRecipient: String = "",
    val popLiteIp: String = "",
    val isLineEnabled: Boolean = false,
    val isEmailEnabled: Boolean = false,
    val isPopLiteEnabled: Boolean = false
)
