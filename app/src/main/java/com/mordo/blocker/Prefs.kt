package com.mordo.blocker

import android.content.Context
import org.json.JSONObject

object Prefs {
    private const val NAME = "mordo_native"
    private const val WEB_CONFIG = "web_config"
    private const val ENABLED = "enabled"
    private const val EXTRA_BLOCKED = "extra_blocked"
    private const val TOTAL_BLOCKS = "total_blocks"
    private const val SYSTEM_SETUP_UNTIL = "system_setup_until"
    private const val UNINSTALL_BYPASS_UNTIL = "uninstall_bypass_until"

    private fun p(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setWebConfig(c: Context, json: String) {
        p(c).edit().putString(WEB_CONFIG, json).apply()
        runCatching {
            val o = JSONObject(json)
            setEnabled(c, o.optBoolean("onboarded", false))
        }
    }

    fun webConfig(c: Context): String = p(c).getString(WEB_CONFIG, "{}") ?: "{}"
    fun isEnabled(c: Context): Boolean = p(c).getBoolean(ENABLED, false)
    fun setEnabled(c: Context, value: Boolean) = p(c).edit().putBoolean(ENABLED, value).apply()

    fun configuredHosts(c: Context): Set<String> = runCatching {
        val a = JSONObject(webConfig(c)).optJSONArray("hosts") ?: return@runCatching emptySet()
        buildSet { for (i in 0 until a.length()) add(a.optString(i).trim().lowercase().removePrefix("www.")) }
    }.getOrDefault(emptySet())

    fun configuredPackages(c: Context): Set<String> = runCatching {
        val a = JSONObject(webConfig(c)).optJSONArray("packages") ?: return@runCatching emptySet()
        buildSet { for (i in 0 until a.length()) add(a.optString(i).trim()) }
    }.getOrDefault(emptySet())

    fun adultProtection(c: Context): Boolean = runCatching {
        JSONObject(webConfig(c)).optBoolean("adult", true)
    }.getOrDefault(true)

    fun strict(c: Context): Boolean = runCatching {
        JSONObject(webConfig(c)).optBoolean("strict", true)
    }.getOrDefault(true)

    fun extraBlockedApps(c: Context): Set<String> =
        p(c).getStringSet(EXTRA_BLOCKED, emptySet())?.toSet() ?: emptySet()

    fun setPackageBlocked(c: Context, packageName: String, blocked: Boolean) {
        val set = extraBlockedApps(c).toMutableSet()
        if (blocked) set.add(packageName) else set.remove(packageName)
        p(c).edit().putStringSet(EXTRA_BLOCKED, set).apply()
    }

    fun allBlockedPackages(c: Context): Set<String> = configuredPackages(c) + extraBlockedApps(c)

    fun recordBlock(c: Context) {
        p(c).edit().putLong(TOTAL_BLOCKS, totalBlocks(c) + 1).apply()
    }

    fun totalBlocks(c: Context): Long = p(c).getLong(TOTAL_BLOCKS, 0L)

    fun beginSystemSetup(c: Context, minutes: Int = 3) {
        p(c).edit().putLong(SYSTEM_SETUP_UNTIL, System.currentTimeMillis() + minutes * 60_000L).apply()
    }

    fun systemSetupActive(c: Context): Boolean =
        System.currentTimeMillis() < p(c).getLong(SYSTEM_SETUP_UNTIL, 0L)

    fun beginUninstallBypass(c: Context, minutes: Int = 2) {
        p(c).edit().putLong(UNINSTALL_BYPASS_UNTIL, System.currentTimeMillis() + minutes * 60_000L).apply()
    }

    fun uninstallBypassActive(c: Context): Boolean =
        System.currentTimeMillis() < p(c).getLong(UNINSTALL_BYPASS_UNTIL, 0L)
}
