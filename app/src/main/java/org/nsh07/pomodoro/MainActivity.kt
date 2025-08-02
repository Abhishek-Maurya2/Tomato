package org.nsh07.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.NavItem
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchViewModel
import org.nsh07.pomodoro.ui.theme.ZingTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels(factoryProducer = { TimerViewModel.Factory })
    private val statsViewModel: StatsViewModel by viewModels(factoryProducer = { StatsViewModel.Factory })
    private val stopwatchViewModel: StopwatchViewModel by viewModels(factoryProducer = { StopwatchViewModel.Factory })
    private val settingsViewModel: SettingsViewModel by viewModels(factoryProducer = { SettingsViewModel.Factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isSystemTheme = settingsViewModel.isSystemTheme
            val isDarkTheme = settingsViewModel.isDarkTheme
            val systemDarkTheme = isSystemInDarkTheme()
            
            val shouldUseDarkTheme = if (isSystemTheme) systemDarkTheme else isDarkTheme
            
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
            )
        )
    }
}