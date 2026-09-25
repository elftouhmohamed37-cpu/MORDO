package com.mordo.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class BlockAccessibilityService : AccessibilityService() {
    private lateinit var overlay: BlockOverlayManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = BlockOverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::overlay.isInitialized || event == null) return
        Prefs.applyPendingUnlockIfDue(this)
        if (!Prefs.isEnabled(this)) { overlay.hide(); return }

        val pkg = event.packageName?.toString().orEmpty()
        if (pkg.isBlank() || pkg == packageName) { overlay.hide(); return }

        val text = buildString {
            event.text?.forEach { append(it).append('\n') }
            event.contentDescription?.let { append(it).append('\n') }
            append(NodeTextExtractor.extract(rootInActiveWindow))
        }

        if (BlockEngine.shouldBlock(this, pkg, text)) overlay.show() else overlay.hide()
    }

    override fun onInterrupt() { if (::overlay.isInitialized) overlay.hide() }
    override fun onDestroy() { if (::overlay.isInitialized) overlay.hide(); super.onDestroy() }
}