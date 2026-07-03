package com.africopay.pos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.africopay.pos.data.local.db.dao.*
import com.africopay.pos.data.local.db.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        ReceiptEntity::class,
        MerchantProfileEntity::class,
        HardwareDiagnosticEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AfricoPayDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun merchantProfileDao(): MerchantProfileDao
    abstract fun hardwareDiagnosticDao(): HardwareDiagnosticDao

    companion object {
        const val DATABASE_NAME = "africopay_db"
    }
}
