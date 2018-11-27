package com.cbruegg.mensaupb.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.model.UserType

/**
 * Activity managing the user preferences.
 */
class PreferenceActivity : AppCompatActivity() {

    companion object {
        val KEY_PREF_USER_TYPE = "user_type_preference"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PreferenceFragment())
            .commit()
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
