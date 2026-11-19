package com.maturia.bookaccounting.data

import android.content.Context
import com.maturia.bookaccounting.model.Book
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

object BookStorage {

    private const val FILE_NAME = "books.json"

    private const val KEY_ID = "id"
    private const val KEY_FIO = "fio"
    private const val KEY_TITLE = "title"
    private const val KEY_PHONE = "phone"
    private const val KEY_DUE_DATE = "dueDate"

    fun save(context: Context, books: List<Book>) {
        try {
            val jsonArray = JSONArray()

            books.forEach { book ->
                jsonArray.put(book.toJson())
            }

            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(jsonArray.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(context: Context): MutableList<Book> {
        val books = mutableListOf<Book>()

        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return books

            val jsonString = context.openFileInput(FILE_NAME)
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                books.add(jsonArray.getJSONObject(i).toBook())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return books
    }

    private fun Book.toJson(): JSONObject = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_FIO, fio)
        put(KEY_TITLE, title)
        put(KEY_PHONE, phone)
        put(KEY_DUE_DATE, dueDate.toString())
    }

    private fun JSONObject.toBook(): Book = Book(
        id = getInt(KEY_ID),
        fio = getString(KEY_FIO),
        title = getString(KEY_TITLE),
        phone = getString(KEY_PHONE),
        dueDate = LocalDate.parse(getString(KEY_DUE_DATE))
    )
}