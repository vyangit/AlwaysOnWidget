package vyan.alwaysonwidget.services

import android.annotation.TargetApi
import android.content.*
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@TargetApi(24)
class StayAwakeQSTileService : TileService() {
    private lateinit var stayAwakeService: StayAwakeService

    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(className: ComponentName?) {
            isBound = false
        }

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as StayAwakeService.StayAwakeBinder
            stayAwakeService = binder.getService()
            isBound = true
        }
    }
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                StayAwakeService.ACTION_STAY_AWAKE_STATE_CHANGED -> {
                    updateTile(
                        intent.getBooleanExtra(
                            StayAwakeService.EXTRA_STAY_AWAKE_STATE,
                            false
                        )
                    )
                }
            }
        }
    }

    override fun onStartListening() {
        // Register receiver to receive updates from service
        IntentFilter().apply {
            addAction(StayAwakeService.ACTION_STAY_AWAKE_STATE_CHANGED)
        }.also { filter ->
            applicationContext.registerReceiver(updateReceiver, filter)
        }

        // Set to on or off depending if service is pingable/marshaled
        val isUp = pingStayAwakeServiceUp()
        if (!isUp) updateTile(false)
    }

//    override fun onStopListening() {
//        val context = applicationContext
//        if (isBound) context.unbindService(connection)
//        isBound = false
//        context.unregisterReceiver(updateReceiver)
//    }

    override fun onClick() {
        if (!isBound) {
            startForegroundStayAwakeService(applicationContext)
        } else {
            stayAwakeService.toggleStayAwake()
        }
    }

    private fun startForegroundStayAwakeService(context: Context) {
        Intent(context, StayAwakeService::class.java).apply {
            putExtra(StayAwakeService.EXTRA_START_AWAKE_FLAG, true)
        }.also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun pingStayAwakeServiceUp(): Boolean {
        val intent = Intent(applicationContext, StayAwakeService::class.java)
        return bindService(intent, connection, 0)
    }

    private fun updateTile(isOn: Boolean) {
        val tile = qsTile
        if (isOn) tile.state = Tile.STATE_ACTIVE else
            tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }
}