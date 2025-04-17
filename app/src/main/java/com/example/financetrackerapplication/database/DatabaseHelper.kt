package com.example.financetrackerapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.financetrackerapplication.models.Transaction

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "transactions.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DATE = "date"

        const val TABLE_ACCOUNTS = "linked_accounts"
        const val COLUMN_ACCOUNT_ID = "account_id"
        const val COLUMN_ACCOUNT_NAME = "account_name"
        const val COLUMN_INSTITUTION_NAME = "institution_name"

        const val TABLE_BUDGETS = "budgets"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_DATE TEXT
            )
        """.trimIndent()

        val createAccountsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNTS (
                $COLUMN_ACCOUNT_ID TEXT PRIMARY KEY,
                $COLUMN_ACCOUNT_NAME TEXT,
                $COLUMN_INSTITUTION_NAME TEXT
            )
        """.trimIndent()

        val createBudgetsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                month INTEGER,
                year INTEGER,
                amount REAL,
                PRIMARY KEY (month, year)
            )
        """.trimIndent()

        db.execSQL(createTransactionsTable)
        db.execSQL(createAccountsTable)
        db.execSQL(createBudgetsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        onCreate(db)
    }

    fun insertTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, transaction.name)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_DATE, transaction.date)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun getTransactionsByMonthYear(month: Int, year: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_TRANSACTIONS
        WHERE strftime('%m', $COLUMN_DATE) = ?
        AND strftime('%Y', $COLUMN_DATE) = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(String.format("%02d", month), year.toString()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))

            transactions.add(Transaction(id.toString(), amount, date, name))
        }
        cursor.close()
        return transactions
    }

    fun insertOrUpdateBudget(month: Int, year: Int, amount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("month", month)
            put("year", year)
            put("amount", amount)
        }
        return db.insertWithOnConflict(TABLE_BUDGETS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getBudget(month: Int, year: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT amount FROM $TABLE_BUDGETS WHERE month = ? AND year = ?",
            arrayOf(month.toString(), year.toString())
        )
        val amount = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return amount
    }

    fun insertLinkedAccount(accountId: String, accountName: String, institutionName: String): Boolean {
        if (isAccountLinked(accountId)) return false

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ACCOUNT_ID, accountId)
            put(COLUMN_ACCOUNT_NAME, accountName)
            put(COLUMN_INSTITUTION_NAME, institutionName)
        }

        return db.insert(TABLE_ACCOUNTS, null, values) != -1L
    }

    fun isAccountLinked(accountId: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_ACCOUNTS WHERE $COLUMN_ACCOUNT_ID = ?",
            arrayOf(accountId)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getAllLinkedAccounts(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ACCOUNT_NAME FROM $TABLE_ACCOUNTS", null)
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))

            transactions.add(Transaction(id.toString(), amount, date, name))
        }

        cursor.close()
        return transactions
    }

    fun clearAllLinkedAccounts() {
        writableDatabase.execSQL("DELETE FROM $TABLE_ACCOUNTS")
    }
}
