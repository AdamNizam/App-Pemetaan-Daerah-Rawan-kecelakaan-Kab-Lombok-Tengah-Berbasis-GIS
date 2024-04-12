package com.example.map_gis

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BackgroundService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Lakukan operasi yang diperlukan di sini
        return START_STICKY
    }
}
