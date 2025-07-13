package com.reelblocker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.reelblocker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReelBlockerService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isInstagramActive = false
    private var isServiceEnabled = false
    private lateinit var prefs: SharedPreferences
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val OVERLAY_WIDTH_DP = 60
        private const val OVERLAY_HEIGHT_DP = 60
        
        fun setServiceEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("reel_blocker_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("reel_blocker_prefs", Context.MODE_PRIVATE)
        isServiceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.packageNames = arrayOf(INSTAGRAM_PACKAGE)
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isServiceEnabled) return
        
        val packageName = event.packageName?.toString() ?: return
        
        if (packageName == INSTAGRAM_PACKAGE) {
            if (!isInstagramActive) {
                isInstagramActive = true
                showOverlay()
            }
            
            // Try to find and analyze the navigation bar to position overlay more precisely
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                analyzeInstagramUI(event.source)
            }
        } else {
            if (isInstagramActive) {
                isInstagramActive = false
                hideOverlay()
            }
        }
    }
    
    private fun analyzeInstagramUI(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo ?: return
        
        // Look for the bottom navigation bar
        // This is a simplified approach - in a real app, you'd need to analyze the view hierarchy
        // more thoroughly to find the exact position of the Reels tab
        
        // For now, we'll just place the overlay at the bottom center of the screen
        // In a real implementation, you'd want to traverse the view hierarchy to find
        // the exact coordinates of the Reels tab
    }
    
    private fun showOverlay() {
        if (overlayView != null) return
        
        val view = View(this)
        view.setBackgroundResource(R.drawable.overlay_background)
        
        val params = WindowManager.LayoutParams(
            dpToPx(OVERLAY_WIDTH_DP),
            dpToPx(OVERLAY_HEIGHT_DP),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        // Position at the bottom center (where Instagram Reels tab typically is)
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        
        // Handle touch events to show toast when user tries to tap
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showBlockedToast()
            }
            true
        }
        
        windowManager?.addView(view, params)
        overlayView = view
    }
    
    private fun hideOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
    
    private fun showBlockedToast() {
        serviceScope.launch {
            Toast.makeText(this@ReelBlockerService, "Reels blocked 👋", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    override fun onInterrupt() {
        // Not used
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }
    
    override fun onUnbind(intent: Intent?): Boolean {
        hideOverlay()
        return super.onUnbind(intent)
    }
}