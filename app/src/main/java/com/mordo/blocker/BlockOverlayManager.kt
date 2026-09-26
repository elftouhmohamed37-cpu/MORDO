package com.mordo.blocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockOverlayManager(private val service: AccessibilityService) {
    private val wm = service.getSystemService(WindowManager::class.java)
    private var overlay: View? = null
    private var reasonView: TextView? = null

    fun show(reason: String) {
        if (overlay != null) { reasonView?.text = reason; return }
        val root = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(42, 58, 42, 58)
            setBackgroundColor(Color.rgb(5, 5, 5))
        }
        root.addView(TextView(service).apply {
            text = "Ⓜ  M O R D O"; textSize = 17f; setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            letterSpacing = .12f
        })
        root.addView(TextView(service).apply {
            text = "BLOCKED"; textSize = 12f; setTextColor(Color.GRAY)
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            letterSpacing = .22f; setPadding(0, 42, 0, 10)
        })
        root.addView(TextView(service).apply {
            text = "This stays locked."; textSize = 38f; setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
        })
        reasonView = TextView(service).apply {
            text = reason; textSize = 16f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER
            setPadding(0, 16, 0, 36)
        }
        root.addView(reasonView)
        root.addView(Button(service).apply {
            text = "Go home"; textSize = 16f; isAllCaps = false
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = 24f }
            setOnClickListener { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
        }, LinearLayout.LayoutParams(-1, -2))
        root.addView(Button(service).apply {
            text = "Open MORDO"; textSize = 15f; isAllCaps = false
            setTextColor(Color.WHITE); setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                hide()
                service.startActivity(Intent(service, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 12 })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        wm.addView(root, params)
        overlay = root
    }

    fun hide() {
        overlay?.let { runCatching { wm.removeView(it) } }
        overlay = null
        reasonView = null
    }
}
