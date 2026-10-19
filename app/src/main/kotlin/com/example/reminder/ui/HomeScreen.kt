package com.example.reminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reminder.Reminder
import com.example.reminder.ReminderCategory
import com.example.reminder.normalizeForSearch
import com.example.reminder.ui.components.ReminderCard
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Reminder) -> Unit,
    onToggleEnabled: (Reminder, Boolean) -> Unit,
    onStopActiveAlarm: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf<ReminderCategory?>(null) }

    val filtered = reminders.filter { reminder ->
        val matchesQuery = query.isBlank() || listOf(
            reminder.firstName,
            reminder.lastName,
            reminder.message
        ).any { it.normalizeForSearch().contains(query.normalizeForSearch()) }
        val matchesCategory = activeCategory == null || reminder.category == activeCategory
        matchesQuery && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sesli Hatırlatıcı") },
                actions = {
                    IconButton(onClick = onStopActiveAlarm) {
                        Icon(Icons.Default.Stop, contentDescription = "Aktif hatırlatmayı durdur")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClick, icon = {
                Icon(Icons.Default.Add, contentDescription = null)
            }, text = { Text("Yeni Hatırlatıcı") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TodaySummaryCard(reminders) }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ara: isim veya mesaj") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = activeCategory == null,
                        onClick = { activeCategory = null },
                        label = { Text("Tümü") }
                    )
                    ReminderCategory.entries.forEach { category ->
                        FilterChip(
                            selected = activeCategory == category,
                            onClick = {
                                activeCategory = if (activeCategory == category) null else category
                            },
                            label = { Text(category.label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item { EmptyState(hasAnyReminders = reminders.isNotEmpty()) }
            } else {
                items(filtered, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggleEnabled = { onToggleEnabled(reminder, it) },
                        onEdit = { onEditClick(reminder.id) },
                        onDelete = { onDeleteClick(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(reminders: List<Reminder>) {
    val today = remember { isoWeekday(Calendar.getInstance()) }
    val nowMinutes = remember {
        val now = Calendar.getInstance()
        now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    }

    val todaysTimes = reminders
        .filter { it.enabled && it.days.contains(today) }
        .flatMap { reminder -> reminder.times.map { reminder to it } }
        .sortedBy { it.second }

    val next = todaysTimes.firstOrNull { (_, time) -> timeToMinutes(time) >= nowMinutes }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "  Bugün",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = if (todaysTimes.isEmpty()) {
                    "Bugün için planlanmış hatırlatıcı yok."
                } else {
                    "Bugün ${todaysTimes.size} hatırlatıcı planlandı."
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (next != null) {
                val (reminder, time) = next
                Text(
                    text = "Sıradaki: $time — ${reminder.message}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyState(hasAnyReminders: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (hasAnyReminders) {
                "Bu aramaya/filtreye uyan hatırlatıcı yok."
            } else {
                "Henüz hatırlatıcı eklemediniz.\n\"Yeni Hatırlatıcı\" ile başlayın."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun isoWeekday(calendar: Calendar): Int {
    // Calendar.DAY_OF_WEEK is SUNDAY=1..SATURDAY=7; app model uses MONDAY=1..SUNDAY=7.
    val day = calendar.get(Calendar.DAY_OF_WEEK)
    return if (day == Calendar.SUNDAY) 7 else day - 1
}

private fun timeToMinutes(time: String): Int {
    val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: 0 }
    return hour * 60 + minute
}
