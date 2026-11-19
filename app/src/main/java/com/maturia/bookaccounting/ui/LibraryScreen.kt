package com.maturia.bookaccounting.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maturia.bookaccounting.data.BookStorage
import com.maturia.bookaccounting.model.Book
import com.maturia.bookaccounting.ui.components.ReaderCard
import com.maturia.bookaccounting.ui.components.StatChip
import com.maturia.bookaccounting.util.Notifications
import com.maturia.bookaccounting.util.daysLeft
import com.maturia.bookaccounting.util.formatDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val context = LocalContext.current

    // живой список выдач — Compose сам перерисует экран при add/remove
    val books = remember {
        mutableStateListOf<Book>().also { it.addAll(BookStorage.load(context)) }
    }

    // поля формы добавления
    var readerName by remember { mutableStateOf("") }
    var bookName by remember { mutableStateOf("") }
    var readerPhone by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf(LocalDate.now().plusDays(14)) }

    // открыт ли диалог выбора даты
    var pickDate by remember { mutableStateOf(false) }

    // строка поиска
    var query by remember { mutableStateOf("") }

    // меню в шапке и флаг «уведомления включены»
    var menuOpen by remember { mutableStateOf(false) }
    var notifyOn by remember { mutableStateOf(true) }

    // запрос разрешения на уведомления (Android 13+)
    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifyOn = granted
        if (!granted) Toast.makeText(context, "Уведомления отключены", Toast.LENGTH_SHORT).show()
    }

    // добавляет запись из формы в список и сохраняет в файл
    fun saveIssue() {
        if (readerName.isBlank() || bookName.isBlank()) return

        // id вычисляем прямо здесь — незачем держать его в отдельном состоянии
        val newId = if (books.isEmpty()) 1 else books.maxOf { it.id } + 1
        books.add(Book(newId, readerName.trim(), bookName.trim(), readerPhone.trim(), returnDate))

        // чистим форму
        readerName = ""
        bookName = ""
        readerPhone = ""
        returnDate = LocalDate.now().plusDays(14)

        BookStorage.save(context, books)
    }

    // сначала отбираем по поиску, потом сортируем по сроку — так понятнее, чем одной цепочкой
    val filtered = if (query.isBlank()) {
        books
    } else {
        books.filter { it.fio.contains(query, true) || it.title.contains(query, true) }
    }
    val shownBooks = filtered.sortedBy { it.dueDate }

    // счётчики для панели сверху
    val overdueCount = books.count { daysLeft(it.dueDate) < 0 }
    val todayCount = books.count { daysLeft(it.dueDate) == 0 }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Учёт выдач", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Меню")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        Text(
                            "Настройки уведомлений",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenuItem(
                            text = { Text(if (notifyOn) "Уведомления: Вкл" else "Уведомления: Выкл") },
                            onClick = {
                                if (!notifyOn) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notifyOn = true
                                    }
                                } else {
                                    notifyOn = false
                                }
                                menuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Тест уведомления") },
                            onClick = {
                                if (notifyOn) {
                                    Notifications.send(context, "Проверка связи", "Уведомления работают!")
                                } else {
                                    Toast.makeText(context, "Сначала включите уведомления!", Toast.LENGTH_SHORT).show()
                                }
                                menuOpen = false
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        BookStorage.save(context, books)
                        if (notifyOn) {
                            Notifications.send(context, "Данные сохранены", "Все ${books.size} записей записаны.")
                        } else {
                            Toast.makeText(context, "Сохранено!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(
                    "Всего: ${books.size}",
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurface
                )
                StatChip("Просрочено: $overdueCount", Color(0xFFFFCDD2), Color(0xFFB71C1C))
                StatChip("Сегодня: $todayCount", Color(0xFFFFE0B2), Color(0xFFE65100))
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = readerName,
                        onValueChange = { readerName = it },
                        label = { Text("ФИО читателя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bookName,
                        onValueChange = { bookName = it },
                        label = { Text("Название книги") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = readerPhone,
                        onValueChange = { readerPhone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = { pickDate = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Вернуть до: ${formatDate(returnDate)}")
                    }

                    Button(
                        onClick = { saveIssue() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить выдачу")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (shownBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (books.isEmpty()) "Записей пока нет." else "Ничего не найдено",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(shownBooks) { book ->
                        ReaderCard(book) {
                            books.removeAll { it.id == book.id }
                            BookStorage.save(context, books)
                        }
                    }
                }
            }
        }
    }

    // диалог выбора даты
    if (pickDate) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = returnDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        returnDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    pickDate = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { pickDate = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = pickerState) }
    }
}