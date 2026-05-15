package com.example.datamobile.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionDatabase private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("transactions_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private var instance: TransactionDatabase? = null
        fun getInstance(context: Context): TransactionDatabase {
            if (instance == null) {
                instance = TransactionDatabase(context.applicationContext)
            }
            return instance!!
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun insertTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        transactions.add(0, transaction)
        if (transactions.size > 100) {
            transactions.removeAt(transactions.lastIndex)
        }
        val json = gson.toJson(transactions)
        prefs.edit().putString("transactions", json).apply()
    }

    fun deleteTransaction(id: Int) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeAll { it.id == id }
        val json = gson.toJson(transactions)
        prefs.edit().putString("transactions", json).apply()
    }
}
