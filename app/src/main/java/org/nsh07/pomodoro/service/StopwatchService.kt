/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.utils.millisecondsToStopwatchStr

class StopwatchService : Service() {
    
    private val binder = StopwatchBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private var stopwatchJob: Job? = null
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L
    
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        const val CHANNEL_ID = "stopwatch_service_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
    }
    
    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }
    
    fun startStopwatch() {
        if (_isRunning.value) return
        
        _isRunning.value = true
        
        if (startTime == 0L) {
            startTime = SystemClock.elapsedRealtime()
            pauseDuration = 0L
        } else {
            pauseDuration += SystemClock.elapsedRealtime() - pauseTime
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        stopwatchJob = serviceScope.launch {
            while (_isRunning.value) {
                val currentElapsed = SystemClock.elapsedRealtime() - startTime - pauseDuration
                val safeElapsed = maxOf(0L, minOf(currentElapsed, 86400000L))
                
                _elapsedTime.value = safeElapsed
                
                // Update notification every second
                notificationManager.notify(NOTIFICATION_ID, createNotification())
                
                delay(1000)
            }
        }
    }
    
    fun pauseStopwatch() {
        if (!_isRunning.value) return
        
        _isRunning.value = false
        
        if (startTime > 0L) {
            pauseTime = SystemClock.elapsedRealtime()
        }
        
        stopwatchJob?.cancel()
        
        // Update notification to show paused state
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _isRunning.value = false
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        _elapsedTime.value = 0L
        
        stopSelf()
    }
    
    fun getCurrentElapsedTime(): Long = _elapsedTime.value
    
    private fun stopService() {
        stopwatchJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stopwatch Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows stopwatch running in background"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val pauseIntent = Intent(this, StopwatchService::class.java).apply {
            action = if (_isRunning.value) ACTION_PAUSE else ACTION_START
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, StopwatchService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val timeString = millisecondsToStopwatchStr(_elapsedTime.value)
        val statusText = if (_isRunning.value) "Running" else "Paused"
        val pauseButtonText = if (_isRunning.value) "Pause" else "Resume"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stopwatch $statusText")
            .setContentText(timeString)
            .setSmallIcon(R.drawable.clocks)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(
                if (_isRunning.value) R.drawable.baseline_pause else R.drawable.baseline_play_arrow,
                pauseButtonText,
                pausePendingIntent
            )
            .addAction(R.drawable.baseline_stop, "Stop", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopwatchJob?.cancel()
    }
}