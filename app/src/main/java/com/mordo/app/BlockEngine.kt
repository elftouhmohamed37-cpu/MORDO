package com.mordo.app

import android.content.Context

object BlockEngine {
    private val browsers=setOf(
        "com.android.chrome","com.huawei.browser","org.mozilla.firefox",
        "com.microsoft.emmx","com.opera.browser","com.brave.browser",
        "com.sec.android.app.sbrowser","com.duckduckgo.mobile.android"
    )

    fun shouldBlock(context:Context, packageName:String, visibleText:String):Boolean {
        if(!Prefs.isEnabled(context) || packageName==context.packageName) return false

        val blockedApps=Prefs.blockedApps(context)
        if((Prefs.appBlock(context) || Prefs.focusActive(context)) && blockedApps.contains(packageName)) return true

        val text=visibleText.lowercase()
        val keywords=Prefs.keywords(context)

        if(Prefs.keywordBlock(context) && keywords.any { k ->
                val key=k.trim().lowercase()
                key.isNotBlank() && text.contains(key)
            }) return true

        if(Prefs.youtubeBlock(context) && packageName=="com.google.android.youtube"){
            if(keywords.any { k -> k.isNotBlank() && text.contains(k.trim().lowercase()) }) return true
        }

        if(Prefs.adultBlock(context) && (packageName in browsers || looksLikeWeb(text))){
            if(Prefs.domains(context).any { d ->
                    val domain=d.trim().lowercase().removePrefix("www.")
                    domain.isNotBlank() && text.contains(domain)
                }) return true
        }

        return false
    }

    private fun looksLikeWeb(text:String)=
        text.contains("http") || text.contains("www.") || text.contains(".com") ||
        text.contains(".net") || text.contains(".org")
}
