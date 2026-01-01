package com.example.fitsuperapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        
        // Actions
        const val ACTION_START_TIMER = "com.example.fitsuperapp.action.START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.fitsuperapp.action.STOP_TIMER"
        const val EXTRA_DURATION = "com.example.fitsuperapp.extra.DURATION"
        const val EXTRA_TITLE = "com.example.fitsuperapp.extra.TITLE"
        
        // Global state to be observed by UI (simplest way without binding for now)
        private val _timeLeft = MutableStateFlow(0L)
        val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()
        
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
        
        private var currentTitle = "Temporizador"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Temporizador"
                currentTitle = title
                startTimer(duration)
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationMillis: Long) {
        timer?.cancel()
        _isRunning.value = true
        
        startForeground(NOTIFICATION_ID, buildNotification(durationMillis))

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isRunning.value = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                showFinishedNotification()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        _isRunning.value = false
        _timeLeft.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Temporizadores",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones para el temporizador de descanso y HIIT"
                setSound(null, null) // Silent for updates
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(millisLeft: Long): android.app.Notification {
        val seconds = millisLeft / 1000
        val formattedTime = String.format("%02d:%02d", seconds / 60, seconds % 60)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText("Tiempo restante: $formattedTime")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(millisLeft: Long) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(millisLeft))
    }
    
    private fun showFinishedNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText("¡Tiempo terminado!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
            
        manager.notify(NOTIFICATION_ID + 1, notification)
    }
}
