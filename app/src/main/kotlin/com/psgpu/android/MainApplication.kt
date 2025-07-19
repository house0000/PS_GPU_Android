package com.psgpu.android

import android.app.Application
import com.psgpu.android.filter.PSFilter

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PSFilter.init(this)
    }
}