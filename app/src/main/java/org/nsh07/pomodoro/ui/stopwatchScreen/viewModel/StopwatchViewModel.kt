/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen.viewModel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import org.nsh07.pomodoro.utils.millisecondsToStopwatchStr

class StopwatchViewModel : ViewModel() {
    
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
                _elapsedTime.update { currentElapsed }
                
                _stopwatchState.update { currentState ->
                    currentState.copy(
                        elapsedTime = currentElapsed,
                        timeStr = millisecondsToStopwatchStr(currentElapsed)
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
        pauseTime = SystemClock.elapsedRealtime()
        stopwatchJob?.cancel()
    }

    private fun resetStopwatch() {
        stopwatchJob?.cancel()
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        
        _elapsedTime.update { 0L }
        _stopwatchState.update { 
            StopwatchState(
                elapsedTime = 0L,
                timeStr = "00:00",
                isRunning = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StopwatchViewModel()
            }
        }
    }
}
