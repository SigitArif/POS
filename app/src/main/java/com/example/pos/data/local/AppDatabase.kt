package com.example.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pos.data.local.Converters
import com.example.pos.data.local.dao.SalesOrderDao
import com.example.pos.data.local.dao.SalesOrderItemDao

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SalesOrderEntity::class,
        SalesOrderItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun salesOrderItemDao(): SalesOrderItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // First create sales_orders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sales_orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        totalRevenue REAL NOT NULL,
                        totalProfit REAL NOT NULL
                    )
                """)

                // Then create sales_order_items table with proper foreign key
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sales_order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        salesOrderId INTEGER NOT NULL,
                        productId INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        profit REAL NOT NULL
                    )
                """)

//                // Create index for better query performance
//                database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_order_items_salesOrderId ON sales_order_items(salesOrderId)")
//                database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_order_items_productId ON sales_order_items(productId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }
} 