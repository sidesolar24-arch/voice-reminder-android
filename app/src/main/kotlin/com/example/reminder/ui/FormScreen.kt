package com.example.reminder.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reminder.CUSTOM_MELODY_LABEL
import com.example.reminder.Reminder
import com.example.reminder.ReminderCategory
import com.example.reminder.ui.components.CategoryBadge
import java.util.Calendar

private val dayLabels = listOf(1 to "Pzt", 2 to "Sal", 3 to "Çar", 4 to "Per", 5 to "Cum", 6 to "Cmt", 7 to "Paz")
private val presetMelodies = listOf("Klasik", "Yumuşak", "Nazik")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    existing: Reminder?,
    onSave: (Reminder) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var firstName by rememberSaveable { mutableStateOf(existing?.firstName.orEmpty()) }
    var lastName by rememberSaveable { mutableStateOf(existing?.lastName.orEmpty()) }
    var message by rememberSaveable { mutableStateOf(existing?.message.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: ReminderCategory.OTHER) }
    var selectedDays by remember { mutableStateOf(existing?.days ?: emptySet()) }
    val times = remember { mutableStateListOf<String>().apply { existing?.times?.let { addAll(it) } } }
    var selectedMelody by rememberSaveable { mutableStateOf(existing?.melody ?: "Klasik") }
    var soundUri by rememberSaveable { mutableStateOf(existing?.soundUri) }

    val pickAudioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            soundUri = uri.toString()
            selectedMelody = CUSTOM_MELODY_LABEL
        }
    }

    fun displayToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Yeni Hatırlatıcı" else "Hatırlatıcıyı Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            Text("Kategori", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderCategory.entries.forEach { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = { Text(option.label) }
                    )
                }
            }
            CategoryBadge(category = category)

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
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val value = String.format("%02d:%02d", hour, minute)
                            if (!times.contains(value)) times.add(value)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }) {
                    Text("Yeni Saat Ekle")
                }
            }

            Text("Melodi Seçimi", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presetMelodies.forEach { melody ->
                    FilterChip(
                        selected = selectedMelody == melody && soundUri == null,
                        onClick = {
                            selectedMelody = melody
                            soundUri = null
                        },
                        label = { Text(melody) }
                    )
                }
            }
            OutlinedButton(onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) }) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null)
                Text(
                    text = if (soundUri != null) "  Özel ses seçildi (değiştir)" else "  Kendi sesimi seç...",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        firstName.isBlank() -> displayToast("Lütfen adınızı girin.")
                        message.isBlank() -> displayToast("Lütfen bir hatırlatma mesajı yazın.")
                        selectedDays.isEmpty() -> displayToast("Hatırlatma günlerini seçin.")
                        times.isEmpty() -> displayToast("En az bir saat ekleyin.")
                        else -> {
                            val reminder = Reminder(
                                id = existing?.id ?: System.currentTimeMillis(),
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                message = message.trim(),
                                melody = selectedMelody,
                                soundUri = soundUri,
                                category = category,
                                days = selectedDays,
                                times = times.sorted(),
                                enabled = existing?.enabled ?: true
                            )
                            onSave(reminder)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing == null) "Hatırlatıcı Ekle ve Kaydet" else "Değişiklikleri Kaydet")
            }
        }
    }
}
