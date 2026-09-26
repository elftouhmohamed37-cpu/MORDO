package com.mordo.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class BlockAccessibilityService : AccessibilityService() {
    private lateinit var overlay: BlockOverlayManager
    private var lastKey = ""
    private var lastCountAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = BlockOverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::overlay.isInitialized || event == null) return
        val pkg = event.packageName?.toString().orEmpty()
        if (pkg.isBlank() || pkg == packageName) { overlay.hide(); return }

        val text = buildString {
            event.text?.forEach { append(it).append('\n') }
            event.contentDescription?.let { append(it).append('\n') }
            append(NodeTextExtractor.extract(rootInActiveWindow))
        }

        val decision = BlockEngine.check(this, pkg, text)
        if (decision.blocked) {
            val now = System.currentTimeMillis()
            val key = pkg + ":" + decision.reason
            if (key != lastKey || now - lastCountAt > 5_000L) {
                Prefs.recordBlock(this)
                lastKey = key
                lastCountAt = now
            }
            overlay.show(decision.reason)
        } else {
            overlay.hide()
        }
    }

    override fun onInterrupt() { if (::overlay.isInitialized) overlay.hide() }
    override fun onDestroy() { if (::overlay.isInitialized) overlay.hide(); super.onDestroy() }
}
