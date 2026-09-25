package com.mordo.app

import android.content.Context

object BlockEngine {
    private val browsers = setOf(
        "com.android.chrome","com.huawei.browser","org.mozilla.firefox",
        "com.microsoft.emmx","com.opera.browser","com.brave.browser",
        "com.sec.android.app.sbrowser"
    )

    fun shouldBlock(context: Context, packageName: String, visibleText: String): Boolean {
        if (!Prefs.isEnabled(context) || packageName == context.packageName) return false
        if (Prefs.blockedApps(context).contains(packageName)) return true
        val haystack = visibleText.lowercase()
        if (Prefs.keywords(context).any { it.isNotBlank() && haystack.contains(it.trim().lowercase()) }) return true
        if (packageName in browsers || haystack.contains("http") || haystack.contains("www.") || haystack.contains(".com")) {
            if (Prefs.domains(context).any {
                val d=it.trim().lowercase().removePrefix("www.")
                d.isNotBlank() && haystack.contains(d)
            }) return true
        }
        return false
    }
}