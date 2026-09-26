package com.mordo.blocker

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class NativeBridge(private val activity: Activity) {
    @JavascriptInterface
    fun syncConfig(json: String) {
        Prefs.setWebConfig(activity, json)
    }

    @JavascriptInterface
    fun getInstalledApps(): String {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val blocked = Prefs.extraBlockedApps(activity)
        val array = JSONArray()
        val apps = activity.packageManager.queryIntentActivities(intent, 0)
            .map {
                val pkg = it.activityInfo.packageName
                val label = it.loadLabel(activity.packageManager)?.toString()?.trim().orEmpty().ifBlank { pkg }
                Triple(label, pkg, blocked.contains(pkg))
            }
            .filter { it.second != activity.packageName }
            .distinctBy { it.second }
            .sortedBy { it.first.lowercase() }
        for ((label, pkg, isBlocked) in apps) {
            array.put(JSONObject().put("label", label).put("packageName", pkg).put("blocked", isBlocked))
        }
        return array.toString()
    }

    @JavascriptInterface
    fun setPackageBlocked(packageName: String, blocked: Boolean) {
        if (packageName.isBlank() || packageName == activity.packageName) return
        Prefs.setPackageBlocked(activity, packageName, blocked)
    }

    @JavascriptInterface
    fun getSystemStatus(): String {
        val dpm = activity.getSystemService(DevicePolicyManager::class.java)
        val admin = dpm.isAdminActive(ComponentName(activity, MordoDeviceAdminReceiver::class.java))
        return JSONObject()
            .put("accessibility", accessibilityEnabled())
            .put("admin", admin)
            .put("totalBlocks", Prefs.totalBlocks(activity))
            .toString()
    }

    @JavascriptInterface
    fun openAccessibilitySettings() {
        activity.runOnUiThread {
            Prefs.beginSystemSetup(activity)
            activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    @JavascriptInterface
    fun enableDeviceAdmin() {
        activity.runOnUiThread {
            Prefs.beginSystemSetup(activity)
            val admin = ComponentName(activity, MordoDeviceAdminReceiver::class.java)
            activity.startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "MORDO uses Device Admin only to add an intentional step before uninstall. You remain in control and can remove it through MORDO after your chosen protection challenge."
                )
            })
        }
    }

    @JavascriptInterface
    fun setSecure(enabled: Boolean) {
        activity.runOnUiThread {
            if (enabled) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    @JavascriptInterface
    fun requestUninstall() {
        activity.runOnUiThread {
            Prefs.beginUninstallBypass(activity)
            val admin = ComponentName(activity, MordoDeviceAdminReceiver::class.java)
            val dpm = activity.getSystemService(DevicePolicyManager::class.java)
            if (dpm.isAdminActive(admin)) dpm.removeActiveAdmin(admin)
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching {
                    activity.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:" + activity.packageName)))
                }
            }, 900L)
        }
    }

    @JavascriptInterface
    fun openExternal(url: String) {
        activity.runOnUiThread {
            runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        }
    }

    private fun accessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val c = ComponentName(activity, BlockAccessibilityService::class.java)
        return enabled.contains(c.flattenToString(), true) || enabled.contains(c.flattenToShortString(), true)
    }
}
