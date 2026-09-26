package com.mordo.app

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity:Activity(){
    private lateinit var root:LinearLayout
    private var pendingAction:String?=null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume(){
        super.onResume()
        Prefs.applyPendingUnlockIfDue(this)
        render()
    }

    private fun render(){
        val scroll=ScrollView(this).apply{ setBackgroundColor(Theme.BG) }
        root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(34,40,34,60)
        }

        val header=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply{
            text="MORDO"; textSize=31f; setTextColor(Theme.TEXT)
            typeface=Typeface.DEFAULT_BOLD
        },LinearLayout.LayoutParams(0,-2,1f))
        header.addView(Theme.statusPill(this,Prefs.isEnabled(this)))
        root.addView(header)

        root.addView(Theme.sub(this,"Free content blocker & self-control").apply{setPadding(0,6,0,26)})

        val hero=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(28,26,28,26)
            background=Theme.card()
        }
        hero.addView(Theme.sectionTitle(this,
            if(Prefs.isEnabled(this)) "Protection is active" else "Protection is paused"))
        hero.addView(Theme.sub(this,statusSummary()).apply{setPadding(0,8,0,18)})
        hero.addView(Theme.primaryButton(this,
            if(Prefs.isEnabled(this)) "Disable protection" else "Turn protection ON"){
            if(Prefs.isEnabled(this)) requestSensitive("disable_protection")
            else { Prefs.setEnabled(this,true); Toast.makeText(this,"Protection enabled",Toast.LENGTH_SHORT).show(); render() }
        })
        root.addView(hero)

        addSection("BLOCKING")
        addRow("Adult websites", "${Prefs.domains(this).size} blocked domains") {
            startActivity(Intent(this,TextListActivity::class.java).putExtra("type","domains"))
        }
        addRow("Blocked keywords", "${Prefs.keywords(this).size} words & phrases") {
            startActivity(Intent(this,TextListActivity::class.java).putExtra("type","keywords"))
        }
        addRow("Blocked apps", "${Prefs.blockedApps(this).size} selected apps") {
            startActivity(Intent(this,ManageAppsActivity::class.java))
        }
        addRow("Content protection settings",
            "Websites · keywords · apps · YouTube protection"){
            startActivity(Intent(this,ProtectionSettingsActivity::class.java))
        }

        addSection("SELF-CONTROL")
        addRow("Protection mode", modeLabel()){
            startActivity(Intent(this,ProtectionModeActivity::class.java))
        }
        addRow("Focus Mode", focusLabel()){
            startActivity(Intent(this,FocusActivity::class.java))
        }

        addSection("SYSTEM PROTECTION")
        addRow("Accessibility service",
            if(isAccessibilityEnabled()) "Enabled" else "Required — tap to enable"){
            showAccessibilityDisclosure()
        }
        addRow("Removal protection",
            if(isAdminActive()) "Device Admin active" else "Optional — add uninstall friction"){
            requestAdmin()
        }

        root.addView(Theme.sub(this,
            "MORDO processes blocking rules on this device. Accessibility is used only to detect the current app and visible content needed for blocking.").apply{
            setPadding(4,30,4,0)
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addSection(title:String){
        root.addView(Theme.sub(this,title).apply{
            setPadding(4,30,4,12); typeface=Typeface.DEFAULT_BOLD
        })
    }

    private fun addRow(title:String,sub:String,click:()->Unit){
        root.addView(Theme.row(this,title,sub,click))
        root.addView(Theme.spacer(this,12))
    }

    private fun statusSummary():String{
        val parts=mutableListOf<String>()
        if(Prefs.adultBlock(this)) parts+="adult sites"
        if(Prefs.keywordBlock(this)) parts+="keywords"
        if(Prefs.appBlock(this)) parts+="apps"
        if(Prefs.youtubeBlock(this)) parts+="YouTube"
        return if(parts.isEmpty()) "No blocking categories are enabled"
        else "Blocking: "+parts.joinToString(" · ")
    }

    private fun modeLabel()=when(Prefs.mode(this)){
        Prefs.MODE_FRIEND->"Friend Mode — changes require a PIN"
        Prefs.MODE_DELAY->"Time Delay — ${Prefs.delayMinutes(this)} min wait"
        else->"Myself Mode — timed typing challenge"
    }

    private fun focusLabel():String{
        if(!Prefs.focusActive(this)) return "Not running"
        val mins=((Prefs.focusUntil(this)-System.currentTimeMillis())/60000L+1).coerceAtLeast(1)
        return "Active — about $mins min remaining"
    }

    private fun requestSensitive(action:String){
        if(Prefs.settingsUnlocked(this)){ completeSensitive(action); return }
        pendingAction=action
        startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
    }

    private fun completeSensitive(action:String){
        when(action){
            "disable_protection"->{ Prefs.setEnabled(this,false); Toast.makeText(this,"Protection disabled",Toast.LENGTH_SHORT).show() }
        }
        render()
    }

    private fun showAccessibilityDisclosure(){
        AlertDialog.Builder(this)
            .setTitle("Enable MORDO blocking")
            .setMessage("MORDO needs Accessibility access to detect the current app and visible text on screen so it can block websites, keywords and selected apps. MORDO does not use this permission to click, type, purchase, message, or control the device for you. Blocking data stays on this device.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Open settings"){_,_->startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
            .show()
    }

    private fun requestAdmin(){
        val admin=ComponentName(this,MordoDeviceAdminReceiver::class.java)
        if(isAdminActive()){
            Toast.makeText(this,"Removal protection is already active",Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply{
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,admin)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Adds an extra deliberate step before MORDO can be removed. You can still deactivate it from Android settings.")
        })
    }

    private fun isAdminActive():Boolean{
        val dpm=getSystemService(DevicePolicyManager::class.java)
        return dpm.isAdminActive(ComponentName(this,MordoDeviceAdminReceiver::class.java))
    }

    private fun isAccessibilityEnabled():Boolean{
        val enabled=Settings.Secure.getString(contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?:return false
        val c=ComponentName(this,BlockAccessibilityService::class.java)
        return enabled.contains(c.flattenToString(),true)||enabled.contains(c.flattenToShortString(),true)
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK){
            pendingAction?.let{completeSensitive(it)}
            pendingAction=null
        }
    }

    companion object{ const val REQ_GATE=700 }
}
