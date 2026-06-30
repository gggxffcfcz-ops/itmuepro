package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY assetCode ASC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: Int): Asset?

    @Query("SELECT * FROM assets WHERE assetCode = :code LIMIT 1")
    suspend fun getAssetByCode(code: String): Asset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset)

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY reportedDate DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Int): Ticket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket)

    @Update
    suspend fun updateTicket(ticket: Ticket)

    @Delete
    suspend fun deleteTicket(ticket: Ticket)

    @Query("SELECT COUNT(*) FROM tickets WHERE status = :status")
    fun getTicketsCountByStatus(status: String): Flow<Int>
}

@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts ORDER BY partCode ASC")
    fun getAllSpareParts(): Flow<List<SparePart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSparePart(part: SparePart)

    @Update
    suspend fun updateSparePart(part: SparePart)

    @Delete
    suspend fun deleteSparePart(part: SparePart)
}

@Dao
interface NotificationConfigDao {
    @Query("SELECT * FROM notification_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<NotificationConfig?>

    @Query("SELECT * FROM notification_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): NotificationConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: NotificationConfig)
}
