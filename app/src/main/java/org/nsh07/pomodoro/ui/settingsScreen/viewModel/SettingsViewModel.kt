/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.ZingApplication
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
class SettingsViewModel(
    private val preferenceRepository: AppPreferenceRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {
    val focusTimeTextFieldState =
        TextFieldState((timerRepository.focusTime / 60000).toString())
    val shortBreakTimeTextFieldState =
        TextFieldState((timerRepository.shortBreakTime / 60000).toString())
    val longBreakTimeTextFieldState =
        TextFieldState((timerRepository.longBreakTime / 60000).toString())

    val sessionsSliderState = SliderState(
        value = timerRepository.sessionLength.toFloat(),
        steps = 4,
        valueRange = 1f..6f,
        onValueChangeFinished = ::updateSessionLength
    )
    // theme mode: 0=system, 1=light, 2=dark
    var isSystemTheme by mutableStateOf(true)
        private set
    var isDarkTheme by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.focusTime = preferenceRepository.saveIntPreference(
                            "focus_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { shortBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.shortBreakTime = preferenceRepository.saveIntPreference(
                            "short_break_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { longBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.longBreakTime = preferenceRepository.saveIntPreference(
                            "long_break_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
    }
    // load saved theme mode
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val mode = preferenceRepository.getIntPreference("theme_mode") ?: 0
            isSystemTheme = (mode == 0)
            isDarkTheme = (mode == 2)
        }
    }

    private fun updateSessionLength() {
        viewModelScope.launch {
            timerRepository.sessionLength = preferenceRepository.saveIntPreference(
                "session_length",
                sessionsSliderState.value.toInt()
            )
        }
    }
    /** Toggle following system theme */
    fun updateSystemTheme(follow: Boolean) {
        isSystemTheme = follow
        if (follow) isDarkTheme = false
        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.saveIntPreference(
                "theme_mode",
                if (follow) 0 else if (isDarkTheme) 2 else 1
            )
        }
    }
    /** Toggle dark theme (disables system follow) */
    fun updateDarkTheme(dark: Boolean) {
        isDarkTheme = dark
        isSystemTheme = false
        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.saveIntPreference(
                "theme_mode",
                if (dark) 2 else 1
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ZingApplication)
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appTimerRepository = application.container.appTimerRepository

                SettingsViewModel(
                    preferenceRepository = appPreferenceRepository,
                    timerRepository = appTimerRepository
                )
            }
        }
    }
}