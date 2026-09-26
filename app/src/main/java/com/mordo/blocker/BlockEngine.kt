package com.mordo.blocker

import android.content.Context

data class BlockDecision(val blocked: Boolean, val reason: String = "")

object BlockEngine {
    private val browsers = setOf(
        "com.android.chrome", "com.huawei.browser", "org.mozilla.firefox",
        "com.microsoft.emmx", "com.opera.browser", "com.brave.browser",
        "com.sec.android.app.sbrowser", "com.duckduckgo.mobile.android"
    )

    private val adultKeywords = setOf(
        "porn", "porno", "pornography", "xxx", "hentai", "rule34",
        "sex video", "adult video", "nude video", "nudes", "pornstar",
        "hardcore", "camgirl", "nsfw porn"
    )

    fun check(context: Context, packageName: String, visibleText: String): BlockDecision {
        if (packageName == context.packageName) return BlockDecision(false)
        if (!Prefs.isEnabled(context)) return BlockDecision(false)

        if (packageName == "com.android.settings" && shouldProtectSettings(context, visibleText)) {
            return BlockDecision(true, "MORDO settings protection")
        }

        if (Prefs.allBlockedPackages(context).contains(packageName)) {
            return BlockDecision(true, "Blocked app")
        }

        val text = visibleText.lowercase()
        val hosts = Prefs.configuredHosts(context)
        val matchedHost = hosts.firstOrNull { host ->
            host.isNotBlank() && (text.contains(host) || text.contains("www." + host))
        }
        if (matchedHost != null) return BlockDecision(true, matchedHost)

        if (Prefs.adultProtection(context) && (packageName in browsers || packageName == "com.google.android.youtube")) {
            val key = adultKeywords.firstOrNull { text.contains(it) }
            if (key != null) return BlockDecision(true, "Adult content")
        }

        return BlockDecision(false)
    }

    private fun shouldProtectSettings(context: Context, visibleText: String): Boolean {
        if (Prefs.systemSetupActive(context) || Prefs.uninstallBypassActive(context)) return false
        val text = visibleText.lowercase()
        if (!text.contains("mordo")) return false
        val danger = listOf(
            "uninstall", "force stop", "deactivate", "device admin", "accessibility", "clear storage", "clear data", "delete app",
            "désinstaller", "désactiver", "forcer l'arrêt", "effacer les données", "accessibilité",
            "إلغاء التثبيت", "إلغاء تثبيت", "إيقاف إجباري", "مسح البيانات", "إمكانية الوصول",
            "desinstalar", "forzar detención", "borrar datos", "accesibilidad"
        )
        return danger.any { text.contains(it) }
    }
}
