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
    version = 7,
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Check if the table exists first
                    val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sales_order_items'")
                    val tableExists = cursor.count > 0
                    cursor.close()
                    
                    if (tableExists) {
                        // Check if the table already has the new columns
                        val tableInfo = database.query("PRAGMA table_info(sales_order_items)")
                        val columnNames = mutableListOf<String>()
                        val nameColumnIndex = tableInfo.getColumnIndex("name")
                        if (nameColumnIndex >= 0) { // Check if the column exists
                            while (tableInfo.moveToNext()) {
                                val columnName = tableInfo.getString(nameColumnIndex)
                                columnNames.add(columnName)
                            }
                        } else {
                            android.util.Log.e("AppDatabase", "Column 'name' not found in PRAGMA table_info result")
                        }
                        tableInfo.close()
                        
                        val needsMigration = !columnNames.contains("productName") || 
                                          !columnNames.contains("productCategory") || 
                                          !columnNames.contains("productCode")
                        
                        if (needsMigration) {
                            // Create temporary table with new schema
                            database.execSQL("""
                                CREATE TABLE IF NOT EXISTS sales_order_items_new (
                                    id TEXT PRIMARY KEY NOT NULL,
                                    salesOrderId TEXT NOT NULL,
                                    productId INTEGER NOT NULL,
                                    quantity INTEGER NOT NULL,
                                    price REAL NOT NULL,
                                    profit REAL NOT NULL,
                                    productCode TEXT,
                                    productName TEXT NOT NULL DEFAULT '',
                                    productCategory TEXT NOT NULL DEFAULT ''
                                )
                            """)

                            // Copy data from old table to new table
                            database.execSQL("""
                                INSERT INTO sales_order_items_new (id, salesOrderId, productId, quantity, price, profit, productCode, productName, productCategory)
                                SELECT id, salesOrderId, productId, quantity, price, profit, NULL, '', ''
                                FROM sales_order_items
                            """)

                            // Drop old table
                            database.execSQL("DROP TABLE sales_order_items")

                            // Rename new table to original name
                            database.execSQL("ALTER TABLE sales_order_items_new RENAME TO sales_order_items")
                        }
                    } else {
                        // If table doesn't exist, create it from scratch
                        database.execSQL("""
                            CREATE TABLE IF NOT EXISTS sales_order_items (
                                id TEXT PRIMARY KEY NOT NULL,
                                salesOrderId TEXT NOT NULL,
                                productId INTEGER NOT NULL,
                                quantity INTEGER NOT NULL,
                                price REAL NOT NULL,
                                profit REAL NOT NULL,
                                productCode TEXT,
                                productName TEXT NOT NULL DEFAULT '',
                                productCategory TEXT NOT NULL DEFAULT ''
                            )
                        """)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Error during migration 6-7", e)
                    // Fallback: recreate the table if anything goes wrong
                    try {
                        database.execSQL("DROP TABLE IF EXISTS sales_order_items")
                        database.execSQL("""
                            CREATE TABLE sales_order_items (
                                id TEXT PRIMARY KEY NOT NULL,
                                salesOrderId TEXT NOT NULL,
                                productId INTEGER NOT NULL,
                                quantity INTEGER NOT NULL,
                                price REAL NOT NULL,
                                profit REAL NOT NULL,
                                productCode TEXT,
                                productName TEXT NOT NULL DEFAULT '',
                                productCategory TEXT NOT NULL DEFAULT ''
                            )
                        """)
                    } catch (e2: Exception) {
                        android.util.Log.e("AppDatabase", "Failed to recreate table during migration fallback", e2)
                        // Last resort: do nothing and let Room handle it
                    }
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration() // Add fallback option as last resort
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