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
    version = 6,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add basePrice column with default value same as price
                database.execSQL("ALTER TABLE products ADD COLUMN basePrice REAL NOT NULL DEFAULT 0")
                database.execSQL("UPDATE products SET basePrice = price")

                // Add productCode column as nullable
                database.execSQL("ALTER TABLE products ADD COLUMN productCode TEXT")
            }
        }

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

                // Create index for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_order_items_salesOrderId ON sales_order_items(salesOrderId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_order_items_productId ON sales_order_items(productId)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create temporary tables with new schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sales_orders_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        dateTime INTEGER NOT NULL,
                        totalRevenue REAL NOT NULL,
                        totalProfit REAL NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sales_order_items_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        salesOrderId TEXT NOT NULL,
                        productId INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        profit REAL NOT NULL
                    )
                """)

                // Copy data from old tables to new tables
                database.execSQL("""
                    INSERT INTO sales_orders_new (id, dateTime, totalRevenue, totalProfit)
                    SELECT CAST(id AS TEXT), dateTime, totalRevenue, totalProfit
                    FROM sales_orders
                """)

                database.execSQL("""
                    INSERT INTO sales_order_items_new (id, salesOrderId, productId, quantity, price, profit)
                    SELECT CAST(id AS TEXT), CAST(salesOrderId AS TEXT), productId, quantity, price, profit
                    FROM sales_order_items
                """)

                // Drop old tables
                database.execSQL("DROP TABLE sales_orders")
                database.execSQL("DROP TABLE sales_order_items")

                // Rename new tables to original names
                database.execSQL("ALTER TABLE sales_orders_new RENAME TO sales_orders")
                database.execSQL("ALTER TABLE sales_order_items_new RENAME TO sales_order_items")

            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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