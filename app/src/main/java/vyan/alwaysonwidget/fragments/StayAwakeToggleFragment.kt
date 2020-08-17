package vyan.alwaysonwidget.fragments

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import vyan.alwaysonwidget.R
import vyan.alwaysonwidget.receivers.StayAwakeReceiver
import vyan.alwaysonwidget.services.StayAwakeService
import vyan.alwaysonwidget.services.StayAwakeService.Companion.ACTION_STAY_AWAKE_STATE_CHANGED

class StayAwakeToggleFragment : Fragment() {
    // Public vars
    private lateinit var toggleBtnView: FloatingActionButton
    private lateinit var indicatorTxtView: TextView
    private lateinit var stayAwakeService: StayAwakeService

    // Private vars
    private var isBound: Boolean = false
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
    private val broadcastReceiver = object : StayAwakeReceiver() {
        override fun onStayAwakeToggleOn() {
            updateStayAwakeToggleView(true)
        }

        override fun onStayAwakeToggleOff() {
            updateStayAwakeToggleView(false)
        }
    }

    // Methods
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stay_awake_toggle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the view for the toggled off stay awake button toggle
        indicatorTxtView = view.findViewById(R.id.fragment_stay_awake_toggle_indicator_text)
        toggleBtnView = view.findViewById(R.id.fragment_stay_awake_toggle_fab_main_toggle)
        toggleBtnView.setOnClickListener { toggleStayAwakeSetting() }
    }

    // TODO: Add check to see if developer stay awake fn is already in use and handle accordingly
    override fun onResume() {
        super.onResume()

        // Register receiver to receive updates from service
        IntentFilter().apply {
            addAction(ACTION_STAY_AWAKE_STATE_CHANGED)
        }.also { filter ->
            requireContext().registerReceiver(broadcastReceiver, filter)
        }

        // Auto creates and binds/rebinds to stay awake service
        Intent(requireContext(), StayAwakeService::class.java).also { intent ->
            requireContext().startService(intent)
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unregister receiver
        requireContext().unregisterReceiver(broadcastReceiver)

        // Unbind service
        if (isBound) requireContext().unbindService(connection)
        isBound = false
    }

    private fun toggleStayAwakeSetting() {
        stayAwakeService.toggleStayAwake()
    }

    // Sets the UI tint color of the stay awake toggle button and the associated caption text feedback
    private fun updateStayAwakeToggleView(isStayAwakeEnabled: Boolean) {
        if (isStayAwakeEnabled) {
            toggleBtnView.imageTintList = resources.getColorStateList(R.color.colorEnabled, null)
            indicatorTxtView.text = resources.getString(R.string.stay_awake_is_enabled)
        } else {
            toggleBtnView.imageTintList = resources.getColorStateList(R.color.colorDisabled, null)
            indicatorTxtView.text = resources.getString(R.string.stay_awake_is_disabled)
        }
    }

    companion object {
        fun getInstance() = StayAwakeToggleFragment()
    }
}