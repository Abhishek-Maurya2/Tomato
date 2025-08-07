package org.nsh07.pomodoro

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.AppViewModelProvider
import org.nsh07.pomodoro.ui.NavItem
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchViewModel
import org.nsh07.pomodoro.ui.theme.ZingTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel
import org.nsh07.pomodoro.utils.NotificationPermissionHelper

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels { AppViewModelProvider.Factory }
    private val statsViewModel: StatsViewModel by viewModels { AppViewModelProvider.Factory }
    private val stopwatchViewModel: StopwatchViewModel by viewModels { AppViewModelProvider.Factory }
    private val settingsViewModel: SettingsViewModel by viewModels { AppViewModelProvider.Factory }
    
    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
        // The service will still work without notification permission,
        // but notifications won't be shown
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        if (NotificationPermissionHelper.shouldRequestNotificationPermission() &&
            !NotificationPermissionHelper.hasNotificationPermission(this)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        setContent {
            val isSystemTheme = settingsViewModel.isSystemTheme
            val isDarkTheme = settingsViewModel.isDarkTheme
            val systemDarkTheme = isSystemInDarkTheme()
            
            val shouldUseDarkTheme = if (isSystemTheme) systemDarkTheme else isDarkTheme
            
            // Update status bar appearance when theme changes
            LaunchedEffect(shouldUseDarkTheme) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !shouldUseDarkTheme
                }
            }
            
            ZingTheme(darkTheme = shouldUseDarkTheme) {
                AppScreen(
                    stopwatchViewModel = stopwatchViewModel,
                    timerViewModel = timerViewModel, 
                    statsViewModel = statsViewModel
                )
            }
        }
    }

    companion object {
        val screens = listOf(
            NavItem(
                Screen.Tasks,
                R.drawable.baseline_check,
                R.drawable.baseline_check,
                "Tasks"
            ),
            NavItem(
                Screen.Stopwatch,
                R.drawable.clocks,
                R.drawable.clocks,
                "Stopwatch"
            ),
            NavItem(
                Screen.Timer,
                R.drawable.hourglass,
                R.drawable.hourglass_filled,
                "Timer"
            ),
            NavItem(
                Screen.Stats,
                R.drawable.monitoring,
                R.drawable.monitoring_filled,
                "Stats"
            ),
            NavItem(
                Screen.Settings,
                R.drawable.settings,
                R.drawable.settings_filled,
                "Settings"
            ),
        )
    }
}
