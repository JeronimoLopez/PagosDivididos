package com.android.pagosdivididos

import android.app.Application

class PagosDivididosApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        EventRepository.initialize(this)
    }
}