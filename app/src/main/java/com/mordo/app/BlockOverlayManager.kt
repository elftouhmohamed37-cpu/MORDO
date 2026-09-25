package com.mordo.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockOverlayManager(private val service: AccessibilityService) {
    private val wm = service.getSystemService(WindowManager::class.java)
    private var overlay: View? = null

    fun show() {
        if (overlay != null) return
        val root = LinearLayout(service).apply {
            orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
            setPadding(48,48,48,48); setBackgroundColor(Color.rgb(15,15,17))
        }
        root.addView(TextView(service).apply {
            text="MORDO"; textSize=36f; setTextColor(Color.WHITE)
            typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER
        })
        root.addView(TextView(service).apply {
            text="Blocked content\n\nYou chose to protect yourself from this content."
            textSize=20f; setTextColor(Color.LTGRAY); gravity=Gravity.CENTER
            setPadding(0,36,0,48)
        })
        root.addView(Button(service).apply {
            text="Leave blocked content"; textSize=17f
            setOnClickListener { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
        }, LinearLayout.LayoutParams(-1,-2))

        val p=WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        p.gravity=Gravity.TOP or Gravity.START
        wm.addView(root,p); overlay=root
    }

    fun hide() { overlay?.let { runCatching { wm.removeView(it) }; overlay=null } }
}