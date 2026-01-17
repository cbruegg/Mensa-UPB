package com.cbruegg.mensaupb.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.ActivityPreferenceBinding
import com.cbruegg.mensaupb.model.UserType

/**
 * Activity managing the user preferences.
 */
class PreferenceActivity : AppCompatActivity() {

    companion object {
        const val KEY_PREF_USER_TYPE = "user_type_preference"
    }

    private lateinit var binding: ActivityPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.preferenceToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.preferenceToolbar.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right
            )
            binding.preferenceContainer.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.preferenceContainer.id, PreferenceFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    class PreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, p1: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}

/**
 * Fetch the user type from the preferences file.
 */
val Context.userType: UserType
    get() = UserType.findById(
        PreferenceManager.getDefaultSharedPreferences(
            this
        ).getString(PreferenceActivity.KEY_PREF_USER_TYPE, UserType.STUDENT.id)!!
    )!!
