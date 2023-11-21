package com.example.moapp

import android.app.Application
import android.content.SharedPreferences

class PrefApp: Application() {
    companion object {
        lateinit var prefs: PreferencesUtil
    }

    override fun onCreate() {
        prefs = PreferencesUtil(applicationContext)
        super.onCreate()
    }
}