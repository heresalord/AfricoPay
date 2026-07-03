package com.africopay.pos.data.local.db.dao

import androidx.room.*
import com.africopay.pos.data.local.db.entity.TransactionEntity
import com.africopay.pos.data.local.db.entity.ReceiptEntity
import com.africopay.pos.data.local.db.entity.MerchantProfileEntity
import com.africopay.pos.data.local.db.entity.HardwareDiagnosticEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY timestamp DESC")
    fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE paymentMethod = :method ORDER BY timestamp DESC")
    fun getTransactionsByMethod(method: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayTransactions(startOfDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDay AND status = 'APPROVED'")
    fun getTodayApprovedCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDay AND status != 'APPROVED'")
    fun getTodayDeclinedCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE timestamp >= :startOfDay AND status = 'APPROVED'")
    fun getTodayTotalAmount(startOfDay: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldTransactions(cutoffTimestamp: Long)
}

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY dateTime DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE transactionId = :transactionId")
    suspend fun getReceiptByTransactionId(transactionId: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Query("UPDATE receipts SET printedCount = printedCount + 1, lastPrintedAt = :timestamp WHERE id = :receiptId")
    suspend fun incrementPrintCount(receiptId: String, timestamp: Long)
}

@Dao
interface MerchantProfileDao {

    @Query("SELECT * FROM merchant_profile WHERE id = 'SINGLETON'")
    fun getMerchantProfile(): Flow<MerchantProfileEntity?>

    @Query("SELECT * FROM merchant_profile WHERE id = 'SINGLETON'")
    suspend fun getMerchantProfileOnce(): MerchantProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: MerchantProfileEntity)
}

@Dao
interface HardwareDiagnosticDao {

    @Query("SELECT * FROM hardware_diagnostics ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDiagnostic(): HardwareDiagnosticEntity?

    @Insert
    suspend fun insertDiagnostic(diagnostic: HardwareDiagnosticEntity)

    @Query("DELETE FROM hardware_diagnostics WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldDiagnostics(cutoffTimestamp: Long)
}
