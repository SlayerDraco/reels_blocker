package com.reelblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.reelblocker.service.ReelBlockerService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs: SharedPreferences = context.getSharedPreferences(
                "reel_blocker_prefs", Context.MODE_PRIVATE
            )
            
            val serviceEnabled = prefs.getBoolean("service_enabled", false)
            
            if (serviceEnabled) {
                // Service is enabled, so we should restart it
                ReelBlockerService.setServiceEnabled(context, true)
            }
        }
    }
}