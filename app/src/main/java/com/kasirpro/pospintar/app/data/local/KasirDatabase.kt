package com.kasirpro.pospintar.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        BusinessEntity::class,
        BranchEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        DebtEntity::class,
        CustomerEntity::class,
        StockHistoryEntity::class,
        PromoEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class KasirDatabase : RoomDatabase() {
    abstract fun kasirDao(): KasirDao

    companion object {
        @Volatile
        private var INSTANCE: KasirDatabase? = null

        fun getDatabase(context: Context): KasirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasirDatabase::class.java,
                    "kasir_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
