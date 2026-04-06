package com.example.myapplication

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = hashMapOf(
            "cloud_name" to "di2aizrt5"
        )

        MediaManager.init(this, config)
    }
}