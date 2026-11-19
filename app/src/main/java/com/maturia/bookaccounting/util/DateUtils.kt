package com.maturia.bookaccounting.util

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
//
// Функции для форматирования даты, времени и цвета
//
fun daysLeft(date: LocalDate): Int =
    ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()

fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

fun statusText(date: LocalDate): String {
    val d = daysLeft(date)
    return when {
        d < 0 -> "Просрочено на ${-d} дн."
        d == 0 -> "Сегодня"
        d == 1 -> "Завтра"
        d <= 7 -> "Через $d дн."
        else -> "До ${formatDate(date)}"
    }
}

fun statusColor(date: LocalDate): Color {
    val d = daysLeft(date)
    return when {
        d < 0 -> Color(0xFFD32F2F)
        d == 0 -> Color(0xFFF57C00)
        d <= 7 -> Color(0xFFFFA000)
        else -> Color(0xFF78909C)
    }
}