/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import org.nsh07.pomodoro.service.StopwatchService
import org.nsh07.pomodoro.utils.millisecondsToStopwatchStr
import org.nsh07.pomodoro.utils.millisecondsToStopwatchFormat

class StopwatchViewModel(
    private val statRepository: StatRepository,
    private val context: Context
) : ViewModel() {
    
    private val _stopwatchState = MutableStateFlow(StopwatchState())
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private var stopwatchJob: Job? = null
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L
    
    // Service connection
    private var stopwatchService: StopwatchService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StopwatchService.StopwatchBinder
            stopwatchService = binder.getService()
            isServiceBound = true
            
            // Sync with service state
            syncWithService()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            stopwatchService = null
            isServiceBound = false
        }
    }
    
    init {
        bindToService()
    }

    fun onAction(action: StopwatchAction) {
        when (action) {
            StopwatchAction.ToggleStopwatch -> toggleStopwatch()
            StopwatchAction.ResetStopwatch -> resetStopwatch()
            StopwatchAction.SaveAndResetStopwatch -> saveAndResetStopwatch()
        }
    }

    private fun bindToService() {
        val intent = Intent(context, StopwatchService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun syncWithService() {
        stopwatchService?.let { service ->
            viewModelScope.launch {
                service.elapsedTime.collect { elapsedTime ->
                    _elapsedTime.value = elapsedTime
                    updateStopwatchState(elapsedTime)
                }
            }
            
            viewModelScope.launch {
                service.isRunning.collect { isRunning ->
                    _stopwatchState.update { currentState ->
                        currentState.copy(isRunning = isRunning)
                    }
                }
            }
        }
    }
    
    private fun updateStopwatchState(elapsedTime: Long) {
        val timeFormat = millisecondsToStopwatchFormat(elapsedTime)
        
        _stopwatchState.update { currentState ->
            currentState.copy(
                elapsedTime = elapsedTime,
                timeStr = millisecondsToStopwatchStr(elapsedTime),
                hoursMinutesStr = timeFormat.hoursMinutesStr,
                secondsStr = timeFormat.secondsStr,
                isOverAnHour = timeFormat.isOverAnHour
            )
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
        stopwatchService?.startStopwatch()
        
        // Start service if not already running
        val intent = Intent(context, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    private fun pauseStopwatch() {
        stopwatchService?.pauseStopwatch()
    }

    private fun resetStopwatch() {
        val currentElapsedTime = stopwatchService?.getCurrentElapsedTime() ?: _stopwatchState.value.elapsedTime
        
        stopwatchService?.resetStopwatch()
        resetStopwatchState()
        
        // Only save to stats if there was meaningful time recorded (at least 1 second)
        if (currentElapsedTime >= 1000L) {
            saveTimeToStats(currentElapsedTime)
        }
    }
    
    private fun saveAndResetStopwatch() {
        val currentElapsedTime = stopwatchService?.getCurrentElapsedTime() ?: _stopwatchState.value.elapsedTime
        
        stopwatchService?.resetStopwatch()
        resetStopwatchState()
        
        // Always save time for explicit save action, even if less than 1 second
        if (currentElapsedTime > 0L) {
            saveTimeToStats(currentElapsedTime)
        }
    }
    
    private fun resetStopwatchState() {
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
        stopwatchJob?.cancel()
        
        // Unbind from service
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ZingApplication)
                val appStatRepository = application.container.statRepository
                
                StopwatchViewModel(appStatRepository, application.applicationContext)
            }
        }
    }
}
