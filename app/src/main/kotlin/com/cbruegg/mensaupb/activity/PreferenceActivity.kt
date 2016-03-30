package com.cbruegg.mensaupb.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
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

val Context.userType: UserType
    get() = UserType.findById(PreferenceManager.getDefaultSharedPreferences(
            this).getString(PreferenceActivity.KEY_PREF_USER_TYPE, UserType.STUDENT.id))!!