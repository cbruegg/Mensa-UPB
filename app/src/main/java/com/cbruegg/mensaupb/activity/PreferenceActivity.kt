package com.cbruegg.mensaupb.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import com.cbruegg.mensaupb.R

class PreferenceActivity : AppCompatActivity() {

    companion object {
        val KEY_PREF_USER_TYPE = "user_type_preference"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, PreferenceFragment())
                .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, p1: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

    }
}