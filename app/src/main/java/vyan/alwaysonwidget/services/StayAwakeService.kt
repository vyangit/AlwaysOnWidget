package vyan.alwaysonwidget.services

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import vyan.alwaysonwidget.MainActivity
import vyan.alwaysonwidget.R

private const val NOTIFICATION_CHANNEL_ID = "STAY_AWAKE_NOTIFICATION_CHANNEL"
private const val NOTIFICATION_ID_FOREGROUND_STATE = 1

@SuppressWarnings("deprecation")
class StayAwakeService : Service() {
    // Private vars
    private lateinit var wakeLock: PowerManager.WakeLock
    private val binder = StayAwakeBinder()
    private val receiver = object :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_TOGGLE_STAY_AWAKE -> toggleStayAwake()
                ACTION_ANNOUNCE_STAY_AWAKE_STATE -> broadcastStayAwakeState()
                ACTION_SCREEN_OFF -> stopSelf()
            }
        }
    }

    // Public vars
    val isAwake: Boolean
        get() = wakeLock.isHeld

    // Associated classes
    inner class StayAwakeBinder : Binder() {
        fun getService(): StayAwakeService = this@StayAwakeService
    }

    // Methods
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.getBooleanExtra(EXTRA_START_AWAKE_FLAG, false)) {
            toggleStayAwake(true)
        } else {
            broadcastStayAwakeState()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        // Init wakelock
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AlwaysOnWidget:WakeLock")
        }

        marshalBroadcastReceiver()
        marshalNotificationChannel() // Android 8 =< introduced notification channels
        runAsForeground()
    }

    override fun onBind(intent: Intent?): IBinder? {
        broadcastStayAwakeState()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        broadcastStayAwakeState()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true // Allows onRebind to be called
    }

    override fun onDestroy() {
        // Reset any app widgets
        setStayAwakeState(false)
        unregisterReceiver(receiver)

        // Release foreground
        stopForeground(true)

        super.onDestroy()
    }

    /**
     * Allows service to toggle the "stay awake" functionality
     *
     */
    fun toggleStayAwake(awakeState: Boolean = !isAwake) {
        setStayAwakeState(awakeState)

        // Update notification
        NotificationManagerCompat.from(this)
            .notify(NOTIFICATION_ID_FOREGROUND_STATE, buildNotification())
    }

    private fun setStayAwakeState(awakeState: Boolean) {
        try {
            if (awakeState) wakeLock.acquire()
            else wakeLock.release()
        } catch (e: Exception) {
            // Do nothing
        }
        broadcastStayAwakeState()
    }

    private fun marshalNotificationChannel() {
        // Set up the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stay Awake Notification Channel"
            val description = "Channel manages StayAwake foreground service notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun marshalBroadcastReceiver() {
        // Setup broadcast receiver
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_TOGGLE_STAY_AWAKE)
            addAction(ACTION_ANNOUNCE_STAY_AWAKE_STATE)
            addAction(ACTION_SCREEN_OFF)
        }
        registerReceiver(receiver, intentFilter)
    }

    private fun runAsForeground() {
        // Build the foreground notification
        val foregroundNotice = buildNotification(false)

        // Declare and start service as foreground
        startForeground(NOTIFICATION_ID_FOREGROUND_STATE, foregroundNotice)
    }

    private fun buildNotification(isEnabled: Boolean = isAwake): Notification {
        var contentTitle = "Stay awake is disabled"
        var contentText = "Screen will follow device setting"

        if (isEnabled) {
            contentTitle = "Stay awake is enabled"
            contentText = "Screen will stay dimly awake"
        }

        val pendingIntent = Intent(this, MainActivity::class.java)
            .let { intent ->
                PendingIntent.getActivity(this, 0, intent, 0)
            }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_always_on_toggle_off_24dp) // Tint doesn't matter
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun broadcastStayAwakeState() {
        // Broadcast stay awake toggle states for local app components
        Intent(ACTION_STAY_AWAKE_STATE_CHANGED).apply {
            putExtra(EXTRA_STAY_AWAKE_STATE, wakeLock.isHeld)
        }.also { intent ->
            sendBroadcast(intent)
        }

        /** Update app widgets
         *
         * Not sure why custom actions don't get filtered to
         * app widgets through manifest initialization
         */
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            putExtra(EXTRA_STAY_AWAKE_STATE, wakeLock.isHeld)
        }.also { intent ->
            sendBroadcast(intent)
        }
    }

    companion object { // Constants with public access use
        // Extra for start service
        const val EXTRA_START_AWAKE_FLAG =
            "vyan.alwaysonwidget.services.StayAwakeService.EXTRA_START_AWAKE_FLAG" // Boolean extra

        const val ACTION_TOGGLE_STAY_AWAKE =
            "vyan.alwaysonwidget.services.StayAwakeService.ACTION_TOGGLE_STAY_AWAKE"
        const val ACTION_ANNOUNCE_STAY_AWAKE_STATE =
            "vyan.alwaysonwidget.services.StayAwakeService.ACTION_ANNOUNCE_STAY_AWAKE_STATE"

        const val ACTION_STAY_AWAKE_STATE_CHANGED =
            "vyan.alwaysonwidget.services.StayAwakeService.ACTION_STAY_AWAKE_STATE_CHANGED"
        const val EXTRA_STAY_AWAKE_STATE =
            "vyan.alwaysonwidget.services.StayAwakeService.EXTRA_STAY_AWAKE_STATE" // Boolean extra
    }
}