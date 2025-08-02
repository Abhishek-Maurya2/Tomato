/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen.viewModel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.ZingApplication
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.utils.millisecondsToStopwatchStr
import org.nsh07.pomodoro.utils.millisecondsToStopwatchFormat

class StopwatchViewModel(
    private val statRepository: StatRepository
) : ViewModel() {
    
    private val _stopwatchState = MutableStateFlow(StopwatchState())
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private var stopwatchJob: Job? = null
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    fun onAction(action: StopwatchAction) {
        when (action) {
            StopwatchAction.ToggleStopwatch -> toggleStopwatch()
            StopwatchAction.ResetStopwatch -> resetStopwatch()
            StopwatchAction.SaveAndResetStopwatch -> saveAndResetStopwatch()
        }
    }

    private fun toggleStopwatch() {
        if (_stopwatchState.value.isRunning) {
            pauseStopwatch()
        } else {
            startStopwatch()
        }
    }

    private fun startStopwatch() {
        _stopwatchState.update { currentState ->
            currentState.copy(isRunning = true)
        }

        if (startTime == 0L) {
            startTime = SystemClock.elapsedRealtime()
            pauseDuration = 0L
        } else {
            pauseDuration += SystemClock.elapsedRealtime() - pauseTime
        }

        stopwatchJob = viewModelScope.launch {
            while (_stopwatchState.value.isRunning) {
                val currentElapsed = SystemClock.elapsedRealtime() - startTime - pauseDuration
                
                // Prevent negative time due to clock adjustments or other edge cases
                val safeElapsed = maxOf(0L, currentElapsed)
                
                // Prevent extremely large values that might cause overflow issues
                // Cap at 24 hours (86400000 ms) which is reasonable for a stopwatch session
                val cappedElapsed = minOf(safeElapsed, 86400000L)
                
                _elapsedTime.update { cappedElapsed }
                
                val timeFormat = millisecondsToStopwatchFormat(cappedElapsed)
                
                _stopwatchState.update { currentState ->
                    currentState.copy(
                        elapsedTime = cappedElapsed,
                        timeStr = millisecondsToStopwatchStr(cappedElapsed),
                        hoursMinutesStr = timeFormat.hoursMinutesStr,
                        secondsStr = timeFormat.secondsStr,
                        isOverAnHour = timeFormat.isOverAnHour
                    )
                }
                
                delay(1000)
            }
        }
    }

    private fun pauseStopwatch() {
        _stopwatchState.update { currentState ->
            currentState.copy(isRunning = false)
        }
        
        // Only update pause time if stopwatch was actually running
        if (startTime > 0L) {
            pauseTime = SystemClock.elapsedRealtime()
        }
        
        stopwatchJob?.cancel()
    }

    private fun resetStopwatch() {
        val currentElapsedTime = _stopwatchState.value.elapsedTime
        
        stopwatchJob?.cancel()
        resetStopwatchState()
        
        // Only save to stats if there was meaningful time recorded (at least 1 second)
        // This prevents accidental taps from polluting the stats
        if (currentElapsedTime >= 1000L) {
            saveTimeToStats(currentElapsedTime)
        }
    }
    
    private fun saveAndResetStopwatch() {
        val currentElapsedTime = _stopwatchState.value.elapsedTime
        
        stopwatchJob?.cancel()
        resetStopwatchState()
        
        // Always save time for explicit save action, even if less than 1 second
        // User explicitly requested to save, so respect their intent
        if (currentElapsedTime > 0L) {
            saveTimeToStats(currentElapsedTime)
        }
    }
    
    private fun resetStopwatchState() {
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        
        _elapsedTime.update { 0L }
        _stopwatchState.update { 
            StopwatchState(
                elapsedTime = 0L,
                timeStr = "00:00",
                hoursMinutesStr = "",
                secondsStr = "",
                isOverAnHour = false,
                isRunning = false
            )
        }
    }
    
    private fun saveTimeToStats(elapsedTime: Long) {
        // Validate elapsed time before saving
        val validatedTime = validateElapsedTime(elapsedTime)
        
        if (validatedTime > 0L) {
            viewModelScope.launch {
                try {
                    statRepository.addFocusTime(validatedTime)
                } catch (e: Exception) {
                    // Handle potential database errors gracefully
                    // In a production app, you might want to log this or show a user message
                    // For now, we'll silently handle the error to prevent crashes
                }
            }
        }
    }
    
    private fun validateElapsedTime(elapsedTime: Long): Long {
        return when {
            elapsedTime < 0L -> 0L // Prevent negative time
            elapsedTime > 86400000L -> 86400000L // Cap at 24 hours
            else -> elapsedTime
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Handle case where ViewModel is cleared while stopwatch is running or paused
        // This can happen when the app is destroyed or the user navigates away
        val currentElapsedTime = _stopwatchState.value.elapsedTime
        val isRunning = _stopwatchState.value.isRunning
        
        stopwatchJob?.cancel()
        
        // If stopwatch has meaningful time (running or paused), save it to focus time
        // This ensures we don't lose tracked time when the app is killed or user navigates away
        if (currentElapsedTime >= 1000L) {
            saveTimeToStats(currentElapsedTime)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ZingApplication)
                val appStatRepository = application.container.statRepository
                
                StopwatchViewModel(appStatRepository)
            }
        }
    }
}
