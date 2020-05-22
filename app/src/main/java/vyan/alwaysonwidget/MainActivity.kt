package vyan.alwaysonwidget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import vyan.alwaysonwidget.fragments.IntroductionInstructionsFragment
import vyan.alwaysonwidget.fragments.StayAwakeToggleFragment


const val PERMISSION_REQUEST_WAKE_LOCK = 0

class MainActivity : AppCompatActivity() {
    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.activity_main_container)
    }

    override fun onResume() {
        super.onResume()

        val isPermitted = checkAndRequestWakeLockPermission()

        val activeFragment: Fragment = if (isPermitted)
            StayAwakeToggleFragment.getInstance() else
            IntroductionInstructionsFragment.getInstance()

        setFragment(activeFragment)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WAKE_LOCK) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setFragment(StayAwakeToggleFragment.getInstance())
            } else {
                setFragment(IntroductionInstructionsFragment.getInstance())
                checkAndRequestWakeLockPermission()
            }
        }
    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.activity_main_container, fragment)
        transaction.commit()
    }

    private fun checkAndRequestWakeLockPermission(): Boolean {
        val permit = checkSelfPermission(Manifest.permission.WAKE_LOCK)

        if (permit != PackageManager.PERMISSION_GRANTED) {
            val snackBar = Snackbar.make(
                layout,
                R.string.wake_lock_permission_required,
                Snackbar.LENGTH_INDEFINITE
            )
            snackBar.setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.WAKE_LOCK),
                    PERMISSION_REQUEST_WAKE_LOCK
                )
            }
            snackBar.show()
            return false
        }

        return true // Permission is granted already
    }
}
