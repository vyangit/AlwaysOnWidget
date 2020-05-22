package vyan.alwaysonwidget.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vyan.alwaysonwidget.R

class IntroductionInstructionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_introduction_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun onSettingsButton() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
    }

    companion object {
        fun getInstance() = IntroductionInstructionsFragment()
    }
}