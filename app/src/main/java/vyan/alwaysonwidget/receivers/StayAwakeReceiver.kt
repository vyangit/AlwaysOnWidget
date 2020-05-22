package vyan.alwaysonwidget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vyan.alwaysonwidget.services.StayAwakeService.Companion.EXTRA_STAY_AWAKE_STATE

abstract class StayAwakeReceiver : BroadcastReceiver() {
    /**
     * Calls the correct method depending the on the state change of the stay awake toggle. If
     * overriding make sure to call #super.onReceive
     *
     * @see #onStayAwakeToggleOn
     * @see #onStayAwakeToggleOff
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_STAY_AWAKE_STATE, false)) {
            onStayAwakeToggleOn()
        } else {
            onStayAwakeToggleOff()
        }
    }

    /**
     * Method to call when stay awake state is toggled on
     *
     * @see #onReceive
     */
    abstract fun onStayAwakeToggleOn()

    /**
     * Method to call when stay awake state is toggled off
     *
     * @see #onReceive
     */
    abstract fun onStayAwakeToggleOff()

}