package com.example.fitfusion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitfusion.MainActivity
import com.example.fitfusion.R
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.viewmodel.formatElapsed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class WorkoutForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat(buildNotification(elapsedSeconds = 0L, name = "Entrenamiento activo"))

        observerJob?.cancel()
        observerJob = scope.launch {
            combine(
                ActiveWorkoutManager.session,
                ActiveWorkoutManager.elapsedSeconds,
            ) { session, elapsed -> Pair(session, elapsed) }
                .distinctUntilChanged { (oldSession, oldElapsed), (newSession, newElapsed) ->
                    oldSession?.id == newSession?.id &&
                        oldSession?.name == newSession?.name &&
                        oldSession?.isPaused == newSession?.isPaused &&
                        oldElapsed == newElapsed
                }
                .collect { (session, elapsed) ->
                    if (session == null) {
                        stopSelf()
                        return@collect
                    }
                    val notification = buildNotification(
                        elapsedSeconds = elapsed,
                        name           = session.name.ifBlank { "Entrenamiento activo" },
                        isPaused       = session.isPaused,
                    )
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, notification)
                }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        observerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Entrenamientos",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Notificación persistente del entrenamiento en curso."
                setShowBadge(false)
                enableVibration(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        elapsedSeconds: Long,
        name: String,
        isPaused: Boolean = false,
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val statusLine = if (isPaused) "Sesión en pausa · ${formatElapsed(elapsedSeconds)}"
        else "En curso · ${formatElapsed(elapsedSeconds)}"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dumbbell)
            .setContentTitle(name)
            .setContentText(statusLine)
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "fitfusion_workout"
        private const val NOTIFICATION_ID = 1042

        fun start(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutForegroundService::class.java))
        }
    }
}
