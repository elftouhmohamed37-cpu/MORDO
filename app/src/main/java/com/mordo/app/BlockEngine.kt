package com.mordo.app

import android.content.Context
import android.content.Intent

object BlockEngine {
    private val browsers=setOf(
        "com.android.chrome","com.huawei.browser","org.mozilla.firefox",
        "com.microsoft.emmx","com.opera.browser","com.brave.browser",
        "com.sec.android.app.sbrowser","com.duckduckgo.mobile.android"
    )

    private val essentialPackages=setOf(
        "com.android.systemui",
        "com.android.phone",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.huawei.contacts",
        "com.huawei.message"
    )

    fun shouldBlock(context:Context, packageName:String, visibleText:String):Boolean {
        if(!Prefs.isEnabled(context) || packageName==context.packageName) return false

        if(Prefs.focusActive(context) && !focusAllowed(context,packageName)) return true

        if(Prefs.appBlock(context) && Prefs.blockedApps(context).contains(packageName)) return true

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

    private fun focusAllowed(context:Context,pkg:String):Boolean{
        if(pkg==context.packageName || pkg in essentialPackages) return true
        if(Prefs.focusAllowedApps(context).contains(pkg)) return true
        val homeIntent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val home=context.packageManager.resolveActivity(homeIntent,0)?.activityInfo?.packageName
        if(!home.isNullOrBlank() && pkg==home) return true
        return false
    }

    private fun looksLikeWeb(text:String)=
        text.contains("http") || text.contains("www.") || text.contains(".com") ||
        text.contains(".net") || text.contains(".org")
}
