package com.autoparts.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.autoparts.data.entity.Order
import com.autoparts.data.entity.Product
import com.autoparts.data.entity.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"
        private const val DATABASE_NAME = "autoparts.db"
        private const val DATABASE_VERSION = 8  // Увеличиваем версию для добавления новых товаров

        // Таблица пользователей
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ROLE = "role"
        private const val COLUMN_USER_ADDRESS = "address"
        private const val COLUMN_USER_CREATED_AT = "created_at"
        private const val COLUMN_USER_LAST_LOGIN = "last_login_at"
        private const val COLUMN_USER_AVATAR = "avatar_url"

        // Таблица товаров
        private const val TABLE_PRODUCTS = "products"
        private const val COLUMN_PRODUCT_ID = "id"
        private const val COLUMN_PRODUCT_NAME = "productsName"
        private const val COLUMN_ARTICLE = "article"
        private const val COLUMN_BRAND = "brand"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_IMAGE_URL = "imageUrl"
        private const val COLUMN_VIN_NUMBERS = "vinNumbers"
        private const val COLUMN_COMPATIBLE_CARS = "compatibleCars"
        private const val COLUMN_STOCK = "stock"
        private const val COLUMN_WARRANTY = "warranty"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_DIMENSIONS = "dimensions"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_REVIEWS_COUNT = "reviews_count"
        private const val COLUMN_PRODUCT_CREATED_AT = "created_at"

        // Таблица заказов
        private const val TABLE_ORDERS = "orders"
        private const val COLUMN_ORDER_ID = "id"
        private const val COLUMN_ORDER_USER_ID = "user_id"
        private const val COLUMN_TOTAL_AMOUNT = "total_amount"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_ITEMS_JSON = "items_json"
        private const val COLUMN_DELIVERY_TYPE = "delivery_type"
        private const val COLUMN_PAYMENT_TYPE = "payment_type"
        private const val COLUMN_DELIVERY_ADDRESS = "delivery_address"
        private const val COLUMN_DELIVERY_PHONE = "delivery_phone"
        private const val COLUMN_COMMENT = "comment"

        // Таблица корзины
        private const val TABLE_CART = "cart"
        private const val COLUMN_CART_USER_ID = "user_id"
        private const val COLUMN_CART_PRODUCT_ID = "product_id"
        private const val COLUMN_CART_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Создание базы данных с версией $DATABASE_VERSION")
        createTables(db)
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Обновление базы с версии $oldVersion на $newVersion")

        // Если старая версия меньше 6, пересоздаем таблицу orders с новыми полями
        if (oldVersion < 6) {
            try {
                Log.d(TAG, "Обновление таблицы orders до версии 6")

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")

                // Создаем новую с дополнительными полями
                val createOrdersTable = """
                    CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
                        $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        $COLUMN_ORDER_USER_ID INTEGER NOT NULL,
                        $COLUMN_TOTAL_AMOUNT REAL NOT NULL,
                        $COLUMN_STATUS TEXT DEFAULT 'pending',
                        $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                        $COLUMN_ITEMS_JSON TEXT NOT NULL,
                        $COLUMN_DELIVERY_TYPE TEXT DEFAULT 'pickup',
                        $COLUMN_PAYMENT_TYPE TEXT DEFAULT 'cash',
                        $COLUMN_DELIVERY_ADDRESS TEXT,
                        $COLUMN_DELIVERY_PHONE TEXT,
                        $COLUMN_COMMENT TEXT,
                        FOREIGN KEY ($COLUMN_ORDER_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                    )
                """.trimIndent()

                db.execSQL(createOrdersTable)
                Log.d(TAG, "Таблица orders успешно обновлена до версии 6")

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка обновления таблицы orders: ${e.message}", e)
                // В случае ошибки пересоздаем все таблицы
                dropAllTables(db)
                createTables(db)
                insertInitialData(db)
            }
        }

        // Обновление до версии 8: добавляем новые товары в каталог
        if (oldVersion < 8) {
            try {
                Log.d(TAG, "Обновление до версии 8: добавление новых товаров")
                
                // Очищаем старые товары и добавляем все новые
                db.execSQL("DELETE FROM $TABLE_PRODUCTS")
                Log.d(TAG, "Старые товары удалены")
                
                // Добавляем все новые товары
                insertProductsData(db)
                Log.d(TAG, "Новые товары успешно добавлены")
                
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка обновления товаров: ${e.message}", e)
            }
        }
    }

    private fun dropAllTables(db: SQLiteDatabase) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
            Log.d(TAG, "Все таблицы удалены")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления таблиц: ${e.message}")
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        try {
            // Таблица пользователей
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                    $COLUMN_PHONE TEXT UNIQUE NOT NULL,
                    $COLUMN_PASSWORD TEXT NOT NULL,
                    $COLUMN_NAME TEXT NOT NULL,
                    $COLUMN_ROLE TEXT DEFAULT 'user'
                )
            """.trimIndent()

            // Таблица товаров
            val createProductsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_PRODUCTS (
                    $COLUMN_PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRODUCT_NAME TEXT NOT NULL,
                    $COLUMN_ARTICLE TEXT UNIQUE NOT NULL,
                    $COLUMN_BRAND TEXT NOT NULL,
                    $COLUMN_PRICE REAL NOT NULL,
                    $COLUMN_DESCRIPTION TEXT,
                    $COLUMN_CATEGORY TEXT,
                    $COLUMN_IMAGE_URL TEXT,
                    $COLUMN_VIN_NUMBERS TEXT,
                    $COLUMN_COMPATIBLE_CARS TEXT
                )
            """.trimIndent()

            // Таблица заказов - ВЕРСИЯ 6 С ДОПОЛНИТЕЛЬНЫМИ ПОЛЯМИ
            val createOrdersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
                    $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_ORDER_USER_ID INTEGER NOT NULL,
                    $COLUMN_TOTAL_AMOUNT REAL NOT NULL,
                    $COLUMN_STATUS TEXT DEFAULT 'pending',
                    $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                    $COLUMN_ITEMS_JSON TEXT NOT NULL,
                    $COLUMN_DELIVERY_TYPE TEXT DEFAULT 'pickup',
                    $COLUMN_PAYMENT_TYPE TEXT DEFAULT 'cash',
                    $COLUMN_DELIVERY_ADDRESS TEXT,
                    $COLUMN_DELIVERY_PHONE TEXT,
                    $COLUMN_COMMENT TEXT,
                    FOREIGN KEY ($COLUMN_ORDER_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """.trimIndent()

            // Таблица корзины
            val createCartTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_CART (
                    $COLUMN_CART_USER_ID INTEGER NOT NULL,
                    $COLUMN_CART_PRODUCT_ID INTEGER NOT NULL,
                    $COLUMN_CART_QUANTITY INTEGER DEFAULT 1,
                    PRIMARY KEY ($COLUMN_CART_USER_ID, $COLUMN_CART_PRODUCT_ID),
                    FOREIGN KEY ($COLUMN_CART_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                    FOREIGN KEY ($COLUMN_CART_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PRODUCT_ID)
                )
            """.trimIndent()

            // Выполняем создание таблиц
            db.execSQL(createUsersTable)
            db.execSQL(createProductsTable)
            db.execSQL(createOrdersTable)
            db.execSQL(createCartTable)

            Log.d(TAG, "Все таблицы успешно созданы")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания таблиц: ${e.message}")
            throw e
        }
    }

    // ========== МЕТОДЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ==========
    fun addUser(email: String, phone: String, password: String, name: String, role: String = "user"): Long {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_EMAIL, email)
                put(COLUMN_PHONE, phone)
                put(COLUMN_PASSWORD, password)
                put(COLUMN_NAME, name)
                put(COLUMN_ROLE, role)
            }
            db.insert(TABLE_USERS, null, values)
        } catch (e: Exception) {
            -1
        }
    }

    fun checkUserCredentials(emailOrPhone: String, password: String): User? {
        return try {
            val db = this.readableDatabase
            val query = """
                SELECT * FROM $TABLE_USERS 
                WHERE ($COLUMN_EMAIL = ? OR $COLUMN_PHONE = ?) 
                AND $COLUMN_PASSWORD = ?
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(emailOrPhone, emailOrPhone, password))

            val user = if (cursor.moveToFirst()) {
                User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                )
            } else {
                null
            }
            cursor.close()
            user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка проверки учетных данных: ${e.message}")
            null
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun isPhoneExists(phone: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_PHONE = ?", arrayOf(phone))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updateUser(userId: Int, email: String, phone: String, name: String): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_EMAIL, email)
                put(COLUMN_PHONE, phone)
                put(COLUMN_NAME, name)
            }
            db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString())) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun updateUserWithPassword(userId: Int, email: String, phone: String, name: String, password: String): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_EMAIL, email)
                put(COLUMN_PHONE, phone)
                put(COLUMN_NAME, name)
                put(COLUMN_PASSWORD, password)
            }
            db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString())) > 0
        } catch (e: Exception) {
            false
        }
    }

    // ========== МЕТОДЫ ДЛЯ ТОВАРОВ ==========
    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_PRODUCTS ORDER BY $COLUMN_PRODUCT_ID DESC", null)

            if (cursor.moveToFirst()) {
                do {
                    val product = Product(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        productsName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        article = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTICLE)),
                        brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                        vinNumbers = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIN_NUMBERS)),
                        compatibleCars = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPATIBLE_CARS))
                    )
                    products.add(product)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки товаров: ${e.message}")
        }
        return products
    }

    fun getUserByEmail(email: String): User? {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?",
                arrayOf(email)
            )

            val user = if (cursor.moveToFirst()) {
                User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                )
            } else {
                null
            }
            cursor.close()
            user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения пользователя по email: ${e.message}")
            null
        }
    }

    fun checkUserPassword(userId: Int, password: String): Boolean {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ? AND $COLUMN_PASSWORD = ?",
                arrayOf(userId.toString(), password)
            )
            val isValid = cursor.count > 0
            cursor.close()
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка проверки пароля: ${e.message}")
            false
        }
    }

    fun searchProducts(query: String): List<Product> {
        val products = mutableListOf<Product>()
        val db = this.readableDatabase

        val selection = """
        productsName LIKE ? OR 
        article LIKE ? OR 
        brand LIKE ? OR 
        description LIKE ? OR 
        compatibleCars LIKE ?
    """.trimIndent()

        val searchQuery = "%$query%"
        val selectionArgs = arrayOf(
            searchQuery, searchQuery, searchQuery, searchQuery, searchQuery
        )

        val cursor = db.query(
            "products",
            null,
            selection,
            selectionArgs,
            null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val product = Product(
                    id = it.getInt(it.getColumnIndexOrThrow("id")),
                    productsName = it.getString(it.getColumnIndexOrThrow("productsName")),
                    article = it.getString(it.getColumnIndexOrThrow("article")),
                    brand = it.getString(it.getColumnIndexOrThrow("brand")),
                    price = it.getDouble(it.getColumnIndexOrThrow("price")),
                    description = it.getString(it.getColumnIndexOrThrow("description")),
                    category = it.getString(it.getColumnIndexOrThrow("category")),
                    imageUrl = it.getString(it.getColumnIndexOrThrow("imageUrl")),
                    vinNumbers = it.getString(it.getColumnIndexOrThrow("vinNumbers")),
                    compatibleCars = it.getString(it.getColumnIndexOrThrow("compatibleCars"))
                )
                products.add(product)
            }
        }

        return products
    }

    fun addProduct(product: Product): Long {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_PRODUCT_NAME, product.productsName)
                put(COLUMN_ARTICLE, product.article)
                put(COLUMN_BRAND, product.brand)
                put(COLUMN_PRICE, product.price)
                put(COLUMN_DESCRIPTION, product.description)
                put(COLUMN_CATEGORY, product.category)
                put(COLUMN_IMAGE_URL, product.imageUrl)
                put(COLUMN_VIN_NUMBERS, product.vinNumbers)
                put(COLUMN_COMPATIBLE_CARS, product.compatibleCars)
            }
            db.insert(TABLE_PRODUCTS, null, values)
        } catch (e: Exception) {
            -1
        }
    }

    fun updateProduct(product: Product): Int {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_PRODUCT_NAME, product.productsName)
                put(COLUMN_ARTICLE, product.article)
                put(COLUMN_BRAND, product.brand)
                put(COLUMN_PRICE, product.price)
                put(COLUMN_DESCRIPTION, product.description)
                put(COLUMN_CATEGORY, product.category)
                put(COLUMN_IMAGE_URL, product.imageUrl)
                put(COLUMN_VIN_NUMBERS, product.vinNumbers)
                put(COLUMN_COMPATIBLE_CARS, product.compatibleCars)
            }
            db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(product.id.toString()))
        } catch (e: Exception) {
            0
        }
    }

    fun deleteProduct(id: Int): Int {
        return try {
            val db = this.writableDatabase
            db.delete(TABLE_PRODUCTS, "$COLUMN_PRODUCT_ID = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            0
        }
    }

    // ========== МЕТОДЫ ДЛЯ КОРЗИНЫ ==========
    fun addToCart(userId: Int, productId: Int, quantity: Int = 1): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_CART_USER_ID, userId)
                put(COLUMN_CART_PRODUCT_ID, productId)
                put(COLUMN_CART_QUANTITY, quantity)
            }

            // Проверяем, есть ли уже товар в корзине
            val cursor = db.rawQuery(
                "SELECT $COLUMN_CART_QUANTITY FROM $TABLE_CART WHERE $COLUMN_CART_USER_ID = ? AND $COLUMN_CART_PRODUCT_ID = ?",
                arrayOf(userId.toString(), productId.toString())
            )

            val result = if (cursor.moveToFirst()) {
                // Обновляем количество
                val currentQuantity = cursor.getInt(0)
                val newQuantity = currentQuantity + quantity
                cursor.close()

                val updateValues = ContentValues().apply {
                    put(COLUMN_CART_QUANTITY, newQuantity)
                }
                db.update(TABLE_CART, updateValues,
                    "$COLUMN_CART_USER_ID = ? AND $COLUMN_CART_PRODUCT_ID = ?",
                    arrayOf(userId.toString(), productId.toString())
                ) > 0
            } else {
                // Добавляем новый товар
                cursor.close()
                db.insert(TABLE_CART, null, values) != -1L
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка добавления в корзину: ${e.message}")
            false
        }
    }

    fun getCartItems(userId: Int): List<Pair<Product, Int>> {
        val cartItems = mutableListOf<Pair<Product, Int>>()
        try {
            val db = this.readableDatabase
            val query = """
                SELECT p.*, c.$COLUMN_CART_QUANTITY 
                FROM $TABLE_PRODUCTS p 
                JOIN $TABLE_CART c ON p.$COLUMN_PRODUCT_ID = c.$COLUMN_CART_PRODUCT_ID 
                WHERE c.$COLUMN_CART_USER_ID = ?
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val product = Product(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        productsName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        article = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTICLE)),
                        brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                        vinNumbers = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIN_NUMBERS)),
                        compatibleCars = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPATIBLE_CARS))
                    )
                    val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QUANTITY))
                    cartItems.add(Pair(product, quantity))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки корзины: ${e.message}")
        }
        return cartItems
    }

    fun updateCartItemQuantity(userId: Int, productId: Int, quantity: Int): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_CART_QUANTITY, quantity)
            }
            db.update(TABLE_CART, values,
                "$COLUMN_CART_USER_ID = ? AND $COLUMN_CART_PRODUCT_ID = ?",
                arrayOf(userId.toString(), productId.toString())
            ) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun removeFromCart(userId: Int, productId: Int): Boolean {
        return try {
            val db = this.writableDatabase
            db.delete(TABLE_CART,
                "$COLUMN_CART_USER_ID = ? AND $COLUMN_CART_PRODUCT_ID = ?",
                arrayOf(userId.toString(), productId.toString())
            ) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun clearCart(userId: Int): Boolean {
        return try {
            val db = this.writableDatabase
            db.delete(TABLE_CART, "$COLUMN_CART_USER_ID = ?", arrayOf(userId.toString())) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getCartItemsCount(userId: Int): Int {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT SUM($COLUMN_CART_QUANTITY) FROM $TABLE_CART WHERE $COLUMN_CART_USER_ID = ?",
                arrayOf(userId.toString())
            )
            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    // ========== МЕТОДЫ ДЛЯ ЗАКАЗОВ ==========
    fun createOrderWithDetails(
        userId: Int,
        totalAmount: Double,
        itemsJson: String,
        deliveryType: String,
        paymentType: String,
        address: String,
        phone: String,
        comment: String = ""
    ): Long {
        Log.d(TAG, "createOrderWithDetails: создание заказа для userId=$userId")

        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ORDER_USER_ID, userId)
                put(COLUMN_TOTAL_AMOUNT, totalAmount)
                put(COLUMN_STATUS, "pending")
                put(COLUMN_ITEMS_JSON, itemsJson)
                put(COLUMN_CREATED_AT, System.currentTimeMillis().toString())

                // Используем константы для новых полей
                put(COLUMN_DELIVERY_TYPE, deliveryType)
                put(COLUMN_PAYMENT_TYPE, paymentType)
                put(COLUMN_DELIVERY_ADDRESS, address)
                put(COLUMN_DELIVERY_PHONE, phone)
                put(COLUMN_COMMENT, comment)
            }

            Log.d(TAG, "Вставляем заказ с данными: ${values.keySet()}")

            val orderId = db.insert(TABLE_ORDERS, null, values)

            Log.d(TAG, "Результат вставки: orderId=$orderId")

            if (orderId != -1L) {
                clearCart(userId)
                Log.d(TAG, "Заказ успешно создан с ID: $orderId")
            } else {
                Log.e(TAG, "Ошибка: не удалось создать заказ")
            }

            orderId
        } catch (e: Exception) {
            Log.e(TAG, "Исключение в createOrderWithDetails: ${e.message}", e)
            -1
        }
    }

    fun createOrder(userId: Int, totalAmount: Double, itemsJson: String): Long {
        Log.d(TAG, "createOrder: создание базового заказа для userId=$userId")

        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ORDER_USER_ID, userId)
                put(COLUMN_TOTAL_AMOUNT, totalAmount)
                put(COLUMN_STATUS, "pending")
                put(COLUMN_ITEMS_JSON, itemsJson)
                put(COLUMN_CREATED_AT, System.currentTimeMillis().toString())
            }

            val orderId = db.insert(TABLE_ORDERS, null, values)

            Log.d(TAG, "Результат createOrder: orderId=$orderId")

            if (orderId != -1L) {
                clearCart(userId)
            }

            orderId
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания заказа: ${e.message}", e)
            -1
        }
    }

    fun getAllOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        try {
            val db = this.readableDatabase
            val query = """
            SELECT o.*, 
                   u.$COLUMN_NAME as user_name, 
                   u.$COLUMN_EMAIL as user_email,
                   u.$COLUMN_PHONE as user_phone
            FROM $TABLE_ORDERS o 
            LEFT JOIN $TABLE_USERS u ON o.$COLUMN_ORDER_USER_ID = u.$COLUMN_USER_ID 
            ORDER BY o.$COLUMN_ORDER_ID DESC
        """.trimIndent()

            Log.d(TAG, "Выполняем запрос getAllOrders")
            val cursor = db.rawQuery(query, null)

            Log.d(TAG, "Найдено заказов: ${cursor.count}")

            if (cursor.moveToFirst()) {
                do {
                    // Получаем данные из JOIN-запроса
                    val userName = cursor.getString(cursor.getColumnIndexOrThrow("user_name"))
                    val userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email"))
                    val userPhone = cursor.getString(cursor.getColumnIndexOrThrow("user_phone"))

                    Log.d(TAG, "Заказ ID=${cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID))}, " +
                            "Пользователь: $userName ($userEmail)")

                    val order = Order(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_USER_ID)),
                        userName = userName ?: "Неизвестный",
                        userEmail = userEmail ?: "",
                        userPhone = userPhone ?: "",
                        totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                        itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEMS_JSON)),
                        deliveryType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_TYPE)),
                        paymentType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_TYPE)),
                        deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_ADDRESS)),
                        deliveryPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_PHONE)),
                        comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
                    )
                    orders.add(order)
                } while (cursor.moveToNext())
            } else {
                Log.d(TAG, "Не найдено заказов в БД")
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки заказов: ${e.message}", e)
        }
        return orders
    }

    fun getOrdersByUserId(userId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        try {
            val db = this.readableDatabase
            val query = """
            SELECT o.*, 
                   u.$COLUMN_NAME as user_name, 
                   u.$COLUMN_EMAIL as user_email,
                   u.$COLUMN_PHONE as user_phone
            FROM $TABLE_ORDERS o 
            LEFT JOIN $TABLE_USERS u ON o.$COLUMN_ORDER_USER_ID = u.$COLUMN_USER_ID 
            WHERE o.$COLUMN_ORDER_USER_ID = ? 
            ORDER BY o.$COLUMN_ORDER_ID DESC
        """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(userId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val order = Order(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_USER_ID)),
                        userName = cursor.getString(cursor.getColumnIndexOrThrow("user_name")) ?: "",
                        userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email")) ?: "",
                        userPhone = cursor.getString(cursor.getColumnIndexOrThrow("user_phone")) ?: "",
                        totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                        itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEMS_JSON)),
                        deliveryType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_TYPE)),
                        paymentType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_TYPE)),
                        deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_ADDRESS)),
                        deliveryPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_PHONE)),
                        comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
                    )
                    orders.add(order)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки заказов пользователя: ${e.message}")
        }
        return orders
    }

    fun updateOrderStatus(orderId: Int, status: String): Boolean {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_STATUS, status)
            }
            db.update(TABLE_ORDERS, values, "$COLUMN_ORDER_ID = ?", arrayOf(orderId.toString())) > 0
        } catch (e: Exception) {
            false
        }
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ ОЧИСТКИ ЗАКАЗОВ
    fun deleteAllOrders(): Boolean {
        return try {
            val db = this.writableDatabase
            val deletedRows = db.delete(TABLE_ORDERS, null, null)
            Log.d(TAG, "Удалено заказов: $deletedRows")
            deletedRows > 0
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления всех заказов: ${e.message}")
            false
        }
    }

    fun deleteUserOrders(userId: Int): Boolean {
        return try {
            val db = this.writableDatabase
            val deletedRows = db.delete(TABLE_ORDERS, "$COLUMN_ORDER_USER_ID = ?", arrayOf(userId.toString()))
            Log.d(TAG, "Удалено заказов пользователя $userId: $deletedRows")
            deletedRows > 0
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления заказов пользователя: ${e.message}")
            false
        }
    }

    // ========== ДИАГНОСТИЧЕСКИЕ МЕТОДЫ ==========
    fun checkDatabaseStructure() {
        try {
            val db = this.readableDatabase
            Log.d(TAG, "=== ПРОВЕРКА СТРУКТУРЫ БАЗЫ ДАННЫХ (версия $DATABASE_VERSION) ===")

            // Проверяем таблицу orders
            val cursor = db.rawQuery("PRAGMA table_info($TABLE_ORDERS)", null)
            Log.d(TAG, "Столбцы таблицы $TABLE_ORDERS:")
            while (cursor.moveToNext()) {
                val name = cursor.getString(1)
                val type = cursor.getString(2)
                Log.d(TAG, "  $name ($type)")
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка проверки структуры БД: ${e.message}")
        }
    }

    // ========== ПРИВАТНЫЕ МЕТОДЫ ==========
    private fun insertInitialData(db: SQLiteDatabase) {
        try {
            // Администратор
            val adminValues = ContentValues().apply {
                put(COLUMN_EMAIL, "admin@autoparts.com")
                put(COLUMN_PHONE, "+79991234567")
                put(COLUMN_PASSWORD, "admin123")
                put(COLUMN_NAME, "Администратор")
                put(COLUMN_ROLE, "admin")
            }
            db.insert(TABLE_USERS, null, adminValues)

            // Тестовый пользователь
            val userValues = ContentValues().apply {
                put(COLUMN_EMAIL, "user@example.com")
                put(COLUMN_PHONE, "+79998765432")
                put(COLUMN_PASSWORD, "user123")
                put(COLUMN_NAME, "Иван Иванов")
                put(COLUMN_ROLE, "user")
            }
            db.insert(TABLE_USERS, null, userValues)

            // Добавляем товары
            insertProductsData(db)

            Log.d(TAG, "Начальные данные успешно добавлены")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при инициализации данных: ${e.message}")
        }
    }

    private fun insertProductsData(db: SQLiteDatabase) {
        try {
            // Тестовые товары - расширенный список автозапчастей
            val testProducts = listOf(
                // Фильтры
                Product(0, "Масляный фильтр Mann", "MF-001", "Mann-Filter", 850.0, "Масляный фильтр для двигателей TSI/VW/Audi", "Фильтры", "", "03C115561B,03C115561,03C115561H", "VW Golf, Audi A3, Skoda Octavia"),
                Product(0, "Воздушный фильтр Bosch", "AF-002", "Bosch", 1200.0, "Воздушный фильтр салона с угольным элементом", "Фильтры", "", "1K0819653,1K0819653A,1K0819653B", "VW Polo, Skoda Rapid, Seat Ibiza"),
                Product(0, "Топливный фильтр Mahle", "FF-011", "Mahle", 950.0, "Топливный фильтр тонкой очистки", "Фильтры", "", "1K0201511,1K0201511A", "VW Golf, Passat, Jetta"),
                Product(0, "Салонный фильтр Valeo", "CF-012", "Valeo", 650.0, "Фильтр салона с активированным углем", "Фильтры", "", "1K0819653C,1K0819653D", "VW Polo, Skoda Fabia"),
                
                // Тормозная система
                Product(0, "Тормозные колодки Brembo", "BK-003", "Brembo", 4500.0, "Передние тормозные колодки дисковые", "Тормозная система", "", "8V0615101,8V0615101A,8V0615101B", "Audi A4, VW Passat, Skoda Superb"),
                Product(0, "Тормозные диски TRW", "BD-013", "TRW", 6800.0, "Передние тормозные диски вентилируемые", "Тормозная система", "", "1K0615301,1K0615301A", "VW Golf, Audi A3"),
                Product(0, "Тормозная жидкость DOT4", "BF-014", "Bosch", 450.0, "Тормозная жидкость DOT4 1л", "Тормозная система", "", "Универсальная", "Все модели"),
                Product(0, "Тормозной шланг ATE", "BH-015", "ATE", 1200.0, "Передний тормозной шланг", "Тормозная система", "", "1K0611701,1K0611701A", "VW Polo, Skoda Fabia"),
                
                // Система зажигания
                Product(0, "Свеча зажигания NGK", "SP-004", "NGK", 350.0, "Иридиевая свеча зажигания", "Система зажигания", "", "06H905611,06H905611A,06H905611B", "VW Tiguan, Skoda Kodiaq, Audi Q3"),
                Product(0, "Катушка зажигания Bosch", "IC-016", "Bosch", 3200.0, "Катушка зажигания индивидуальная", "Система зажигания", "", "06H905115,06H905115A", "VW Golf, Passat"),
                Product(0, "Свеча накаливания Beru", "GP-017", "Beru", 850.0, "Свеча накаливания для дизеля", "Система зажигания", "", "N10591607,N10591607A", "VW Golf, Passat TDI"),
                
                // Электрика
                Product(0, "Аккумулятор Varta", "BAT-005", "Varta", 8500.0, "Свинцово-кислотный аккумулятор 60Ач", "Электрика", "", "000915105AC,000915105AD,000915105AE", "Все модели VAG"),
                Product(0, "Генератор Bosch", "GEN-010", "Bosch", 18500.0, "Генератор 140А с регулятором напряжения", "Электрика", "", "03C903023,03C903023A,03C903023B", "Audi Q5, VW Touareg, Porsche Cayenne"),
                Product(0, "Стартер Valeo", "ST-018", "Valeo", 12500.0, "Стартер редукторный 1.4кВт", "Электрика", "", "02T911023,02T911023A", "VW Golf, Passat"),
                Product(0, "Лампа H7 Osram", "BL-019", "Osram", 450.0, "Галогенная лампа H7 55W", "Электрика", "", "Универсальная", "Все модели"),
                Product(0, "Датчик кислорода Bosch", "O2-020", "Bosch", 3200.0, "Лямбда-зонд передний", "Электрика", "", "0258017025,0258017025A", "VW Golf, Passat, Audi A4"),
                
                // Ходовая часть
                Product(0, "ШРУС Lemforder", "CV-006", "Lemforder", 5200.0, "Наружный ШРУС с пыльником", "Ходовая часть", "", "1K0407271,1K0407271A,1K0407271B", "VW Golf, Audi A3, Seat Leon"),
                Product(0, "Амортизатор Sachs", "SH-009", "Sachs", 6800.0, "Передний амортизатор газомасляный", "Ходовая часть", "", "1K0413031,1K0413031A,1K0413031B", "VW Polo, Skoda Fabia, Seat Ibiza"),
                Product(0, "Стойка стабилизатора TRW", "SS-021", "TRW", 1200.0, "Стойка стабилизатора передняя", "Ходовая часть", "", "1K0411315,1K0411315A", "VW Golf, Passat"),
                Product(0, "Рычаг подвески Lemforder", "CA-022", "Lemforder", 4500.0, "Передний нижний рычаг", "Ходовая часть", "", "1K0407151,1K0407151A", "VW Golf, Audi A3"),
                Product(0, "Подшипник ступицы FAG", "HB-023", "FAG", 3200.0, "Подшипник ступицы передний", "Ходовая часть", "", "1K0407621,1K0407621A", "VW Golf, Passat"),
                Product(0, "Пыльник ШРУС Corteco", "CVB-024", "Corteco", 650.0, "Пыльник наружного ШРУС", "Ходовая часть", "", "1K0498101,1K0498101A", "VW Golf, Audi A3"),
                
                // Трансмиссия
                Product(0, "Сцепление LUK", "CL-007", "LUK", 12500.0, "Комплект сцепления (корзина+диск+выжимной)", "Трансмиссия", "", "02T141033,02T141033A,02T141033B", "VW Jetta, Skoda Octavia, Audi A4"),
                Product(0, "Масло трансмиссионное Motul", "TM-025", "Motul", 1200.0, "Масло трансмиссионное 75W-90 1л", "Трансмиссия", "", "Универсальное", "Все модели"),
                Product(0, "Подшипник выжимной Valeo", "TB-026", "Valeo", 1800.0, "Выжимной подшипник сцепления", "Трансмиссия", "", "02T141165,02T141165A", "VW Golf, Passat"),
                
                // Двигатель
                Product(0, "Ремень ГРМ Contitech", "TB-008", "Contitech", 3200.0, "Ремень ГРМ с роликами", "Двигатель", "", "06B109119,06B109119A,06B109119B", "VW Passat, Audi A6, Skoda Superb"),
                Product(0, "Ролик натяжителя ГРМ INA", "TB-027", "INA", 2500.0, "Ролик натяжителя ремня ГРМ", "Двигатель", "", "06B109244,06B109244A", "VW Passat, Audi A6"),
                Product(0, "Помпа водяная Gates", "WP-028", "Gates", 4500.0, "Водяной насос с прокладкой", "Двигатель", "", "06H121026,06H121026A", "VW Golf, Passat TSI"),
                Product(0, "Термостат Wahler", "TH-029", "Wahler", 1800.0, "Термостат двигателя 87°C", "Двигатель", "", "06H121113,06H121113A", "VW Golf, Passat"),
                Product(0, "Ремень приводной Gates", "AB-030", "Gates", 1200.0, "Ремень привода навесного оборудования", "Двигатель", "", "6PK1193,6PK1195", "VW Golf, Passat"),
                Product(0, "Масло моторное Castrol", "EO-031", "Castrol", 1800.0, "Моторное масло 5W-30 4л", "Двигатель", "", "Универсальное", "Все модели"),
                Product(0, "Прокладка ГБЦ Elring", "HG-032", "Elring", 3200.0, "Прокладка головки блока цилиндров", "Двигатель", "", "06H103383,06H103383A", "VW Golf, Passat 1.8T"),
                
                // Система охлаждения
                Product(0, "Радиатор охлаждения Nissens", "RAD-033", "Nissens", 8500.0, "Радиатор системы охлаждения", "Система охлаждения", "", "7M0121251,7M0121251A", "VW Golf, Passat"),
                Product(0, "Расширительный бачок Febi", "ET-034", "Febi", 1200.0, "Бачок расширительный с крышкой", "Система охлаждения", "", "1K0121407,1K0121407A", "VW Golf, Polo"),
                Product(0, "Патрубок охлаждения Gates", "CH-035", "Gates", 650.0, "Патрубок системы охлаждения верхний", "Система охлаждения", "", "1K0121107,1K0121107A", "VW Golf, Passat"),
                
                // Кузов и оптика
                Product(0, "Фара передняя Hella", "HL-036", "Hella", 12500.0, "Фара передняя левая с линзой", "Кузов", "", "1K1941001,1K1941001A", "VW Golf"),
                Product(0, "Бампер передний", "BP-037", "OEM", 18500.0, "Бампер передний в цвет кузова", "Кузов", "", "1K0807211,1K0807211A", "VW Golf"),
                Product(0, "Зеркало боковое Valeo", "SM-038", "Valeo", 3200.0, "Зеркало боковое левое с подогревом", "Кузов", "", "1K1857521,1K1857521A", "VW Golf, Passat"),
                
                // Салон
                Product(0, "Коврик салона WeatherTech", "FM-039", "WeatherTech", 2500.0, "Комплект ковриков салона 4шт", "Салон", "", "Универсальный", "VW Golf, Passat"),
                Product(0, "Чехол на сиденье", "SC-040", "OEM", 3200.0, "Чехол на переднее сиденье", "Салон", "", "Универсальный", "Все модели"),
                
                // Дополнительно
                Product(0, "Дворник передний Bosch", "WB-041", "Bosch", 850.0, "Дворник передний 26\"", "Дополнительно", "", "AER26T", "Универсальный"),
                Product(0, "Щетка стеклоочистителя Valeo", "WB-042", "Valeo", 650.0, "Щетка стеклоочистителя задняя", "Дополнительно", "", "SWF-350", "Универсальная"),
                Product(0, "Антенна Hirschmann", "ANT-043", "Hirschmann", 1200.0, "Антенна наружная магнитная", "Дополнительно", "", "Универсальная", "Все модели")
            )

            testProducts.forEach { product ->
                val values = ContentValues().apply {
                    put(COLUMN_PRODUCT_NAME, product.productsName)
                    put(COLUMN_ARTICLE, product.article)
                    put(COLUMN_BRAND, product.brand)
                    put(COLUMN_PRICE, product.price)
                    put(COLUMN_DESCRIPTION, product.description)
                    put(COLUMN_CATEGORY, product.category)
                    put(COLUMN_IMAGE_URL, product.imageUrl)
                    put(COLUMN_VIN_NUMBERS, product.vinNumbers)
                    put(COLUMN_COMPATIBLE_CARS, product.compatibleCars)
                }
                db.insert(TABLE_PRODUCTS, null, values)
            }

            Log.d(TAG, "Товары успешно добавлены: ${testProducts.size} шт.")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при добавлении товаров: ${e.message}")
        }
    }
}