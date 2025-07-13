package com.reelblocker.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelblocker.service.ReelBlockerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs: SharedPreferences = application.getSharedPreferences(
        "reel_blocker_prefs", Context.MODE_PRIVATE
    )
    
    private val _serviceEnabled = MutableStateFlow(false)
    val serviceEnabled: StateFlow<Boolean> = _serviceEnabled.asStateFlow()
    
    private val _overlayPermissionGranted = MutableStateFlow(false)
    val overlayPermissionGranted: StateFlow<Boolean> = _overlayPermissionGranted.asStateFlow()
    
    private val _accessibilityPermissionGranted = MutableStateFlow(false)
    val accessibilityPermissionGranted: StateFlow<Boolean> = _accessibilityPermissionGranted.asStateFlow()
    
    init {
        _serviceEnabled.value = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        checkPermissions()
    }
    
    fun setServiceEnabled(enabled: Boolean) {
        _serviceEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
        
        // Update service state
        ReelBlockerService.setServiceEnabled(getApplication(), enabled)
    }
    
    fun checkPermissions() {
        viewModelScope.launch {
            // Check overlay permission
            _overlayPermissionGranted.value = Settings.canDrawOverlays(getApplication())
            
            // Check accessibility service
            _accessibilityPermissionGranted.value = isAccessibilityServiceEnabled()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val context = getApplication<Application>()
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        if (accessibilityEnabled == 1) {
            val serviceString = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            return serviceString.contains("${context.packageName}/${context.packageName}.service.ReelBlockerService")
        }
        
        return false
    }
    
    companion object {
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }
}