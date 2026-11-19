package com.maturia.bookaccounting.model

import java.time.LocalDate

data class Book(
    val id: Int,
    val fio: String,
    val title: String,
    val phone: String,
    val dueDate: LocalDate
)