package com.example.mavericksapp

import android.app.Application
import com.airbnb.mvrx.Mavericks

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
    }
}