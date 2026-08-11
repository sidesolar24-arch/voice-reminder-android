package com.example.reminder

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ReminderScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderScreen() {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    val times = remember { mutableStateListOf<String>() }
    var selectedMelody by rememberSaveable { mutableStateOf("Klasik") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val dayLabels = listOf(
        1 to "Pzt",
        2 to "Sal",
        3 to "Çar",
        4 to "Per",
        5 to "Cum",
        6 to "Cmt",
        7 to "Paz"
    )
    val melodyOptions = listOf("Klasik", "Yumuşak", "Nazik")

    LaunchedEffect(Unit) {
        reminders = ReminderStorage.loadReminders(context)
        ReminderScheduler.scheduleAll(context, reminders)
    }

    fun saveReminders(updated: List<Reminder>) {
        reminders = updated
        ReminderStorage.saveReminders(context, updated)
        ReminderScheduler.scheduleAll(context, updated)
    }

    fun displayToast(messageText: String) {
        Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sesli Hatırlatıcı") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Kullanıcı Bilgileri", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Ad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Soyad") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Hatırlatma Mesajı", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Ne hatırlatmak istiyorsunuz?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            Text("Hatırlatma Günleri", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dayLabels.forEach { (day, label) ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }

            Text("Hatırlatma Saatleri", fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                times.forEach { time ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                                Text(text = time, modifier = Modifier.padding(start = 8.dp))
                            }
                            IconButton(onClick = { times.remove(time) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil")
                            }
                        }
                    }
                }
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    val picker = TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val value = String.format("%02d:%02d", hour, minute)
                            if (!times.contains(value)) {
                                times.add(value)
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    picker.show()
                }) {
                    Text("Yeni Saat Ekle")
                }
            }

            Text("Melodi Seçimi", fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Text("Seçili Melodi: $selectedMelody")
                }
                DropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                    melodyOptions.forEach { melody ->
                        DropdownMenuItem(
                            text = { Text(melody) },
                            onClick = {
                                selectedMelody = melody
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                when {
                    firstName.isBlank() -> displayToast("Lütfen adınızı girin.")
                    message.isBlank() -> displayToast("Lütfen bir hatırlatma mesajı yazın.")
                    selectedDays.isEmpty() -> displayToast("Hatırlatma günlerini seçin.")
                    times.isEmpty() -> displayToast("En az bir saat ekleyin.")
                    else -> {
                        val reminder = Reminder(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            message = message.trim(),
                            melody = selectedMelody,
                            days = selectedDays,
                            times = times.sorted()
                        )
                        saveReminders(reminders + reminder)
                        firstName = ""
                        lastName = ""
                        message = ""
                        selectedDays = emptySet()
                        times.clear()
                        displayToast("Hatırlatma kaydedildi ve planlandı.")
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Hatırlatıcı Ekle ve Kaydet")
            }

            if (reminders.isNotEmpty()) {
                Text("Kayıtlı Hatırlatıcılar", fontWeight = FontWeight.Bold)
                reminders.forEach { reminder ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${reminder.firstName} ${reminder.lastName}", fontWeight = FontWeight.Bold)
                                    Text(reminder.message)
                                }
                                Switch(
                                    checked = reminder.enabled,
                                    onCheckedChange = { enabled ->
                                        saveReminders(
                                            reminders.map {
                                                if (it.id == reminder.id) it.copy(enabled = enabled) else it
                                            }
                                        )
                                    }
                                )
                            }
                            Text(
                                "Günler: ${reminder.days.sorted().joinToString(", ") { day ->
                                    dayLabels.firstOrNull { it.first == day }?.second ?: day.toString()
                                }}"
                            )
                            Text("Saatler: ${reminder.times.joinToString(", ")}")
                            Text("Melodi: ${reminder.melody}")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    ReminderScheduler.cancelReminder(context, reminder)
                                    saveReminders(reminders.filter { it.id != reminder.id })
                                }) {
                                    Text("Sil")
                                }
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                context.stopService(Intent(context, ReminderPlayerService::class.java))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Aktif Hatırlatmayı Durdur")
            }

            Text(
                "Hatırlatıcı saatiniz geldiğinde nazik bir Türkçe sesli uyarı verilir ve seçtiğiniz melodi 1 dakika boyunca çalar.",
                color = Color.Gray
            )
        }
    }
}
