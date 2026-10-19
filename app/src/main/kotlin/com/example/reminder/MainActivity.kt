package com.example.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reminder.ui.FormScreen
import com.example.reminder.ui.HomeScreen
import com.example.reminder.ui.theme.ReminderTheme

private const val ROUTE_HOME = "home"
private const val ROUTE_FORM = "form?id={id}"
private const val ARG_ID = "id"
private const val NO_ID = -1L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ReminderApp()
                }
            }
        }
    }
}

@Composable
private fun ReminderApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: reminders still work without it, notification just won't show on Android 13+ */ }

    LaunchedEffect(Unit) {
        reminders = ReminderStorage.loadReminders(context)
        ReminderScheduler.scheduleAll(context, reminders)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun persist(updated: List<Reminder>) {
        reminders = updated
        ReminderStorage.saveReminders(context, updated)
        ReminderScheduler.scheduleAll(context, updated)
    }

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                reminders = reminders,
                onAddClick = { navController.navigate("form?id=$NO_ID") },
                onEditClick = { id -> navController.navigate("form?id=$id") },
                onDeleteClick = { reminder ->
                    ReminderScheduler.cancelReminder(context, reminder)
                    persist(reminders.filter { it.id != reminder.id })
                },
                onToggleEnabled = { reminder, enabled ->
                    persist(reminders.map { if (it.id == reminder.id) it.copy(enabled = enabled) else it })
                },
                onStopActiveAlarm = { context.stopService(Intent(context, ReminderPlayerService::class.java)) }
            )
        }
        composable(
            route = ROUTE_FORM,
            arguments = listOf(navArgument(ARG_ID) { type = NavType.LongType; defaultValue = NO_ID })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(ARG_ID) ?: NO_ID
            val existing = reminders.firstOrNull { it.id == id }
            FormScreen(
                existing = existing,
                onSave = { reminder ->
                    val updated = if (existing != null) {
                        reminders.map { if (it.id == reminder.id) reminder else it }
                    } else {
                        reminders + reminder
                    }
                    persist(updated)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
