package com.cbruegg.mensaupb.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.model.UserType
import com.google.android.material.appbar.MaterialToolbar

/**
 * Activity managing the user preferences.
 */
class PreferenceActivity : AppCompatActivity() {

    companion object {
        const val KEY_PREF_USER_TYPE = "user_type_preference"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_preference)

        val toolbar = findViewById<MaterialToolbar>(R.id.preference_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val root = findViewById<View>(R.id.preference_root)
        val container = findViewById<View>(R.id.preference_container)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            toolbar.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right
            )
            container.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.preference_container, PreferenceFragment())
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
