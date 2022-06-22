package com.osfans.trime.ui.fragments

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.osfans.trime.R
import com.osfans.trime.data.AppPrefs
import com.osfans.trime.data.Config
import com.osfans.trime.ime.core.Trime
import com.osfans.trime.ui.main.MainViewModel

class OtherFragment :
    PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val viewModel : MainViewModel by activityViewModels()
    private val prefs get() = AppPrefs.defaultInstance()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.other_preference)
        findPreference<ListPreference>("other__ui_mode")?.setOnPreferenceChangeListener { _, newValue ->
            val uiMode = when (newValue) {
                "auto" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
            }
            AppCompatDelegate.setDefaultNightMode(uiMode)
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val trime = Trime.getServiceOrNull()
        when (key) {
            "other__show_status_bar_icon" -> {
                if (sharedPreferences?.getBoolean(key, false) == true) {
                    trime?.showStatusIcon(R.drawable.ic_trime_status)
                } else { trime?.hideStatusIcon() }
            }

            "other__clipboard_compare" -> {
                Config.get(context).setClipBoardCompare(
                    sharedPreferences?.getString(key, "")

                )
            }

            "other__clipboard_output" -> {
                Config.get(context).setClipBoardOutput(
                    sharedPreferences?.getString(key, "")
                )
            }
            "other__draft_output" -> {
                Config.get(context).setDraftOutput(
                    sharedPreferences?.getString(key, "")
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.disableTopOptionsMenu()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        updateLauncherIconStatus()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    private fun updateLauncherIconStatus() {
        // Set LauncherAlias enabled/disabled state just before destroying/pausing this activity
        if (prefs.other.showAppIcon) {
            showAppIcon(requireContext())
        } else {
            hideAppIcon(requireContext())
        }
    }

    companion object {
        private const val SETTINGS_ACTIVITY_NAME = "com.osfans.trime.PrefLauncherAlias"

        fun hideAppIcon(context: Context) {
            val pkg: PackageManager = context.packageManager
            pkg.setComponentEnabledSetting(
                ComponentName(context, SETTINGS_ACTIVITY_NAME),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        fun showAppIcon(context: Context) {
            val pkg: PackageManager = context.packageManager
            pkg.setComponentEnabledSetting(
                ComponentName(context, SETTINGS_ACTIVITY_NAME),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
