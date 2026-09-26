package com.mordo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class FocusActivity:Activity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume(){super.onResume();render()}

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(34,42,34,50)}
        root.addView(Theme.title(this,"Focus Mode"))

        if(Prefs.focusActive(this)){
            val mins=((Prefs.focusUntil(this)-System.currentTimeMillis())/60000L+1).coerceAtLeast(1)
            root.addView(Theme.sub(this,"Focus is active. Selected blocked apps remain blocked for about $mins more minute(s).").apply{setPadding(0,8,0,24)})
            root.addView(Theme.primaryButton(this,"Stop Focus early"){
                if(Prefs.settingsUnlocked(this)) stopFocus()
                else startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
            })
        }else{
            root.addView(Theme.sub(this,"Start a focus session. MORDO will turn protection ON and enforce your selected blocked-app list for the whole session.").apply{setPadding(0,8,0,24)})
            duration(root,25);duration(root,45);duration(root,60);duration(root,120);duration(root,240)
        }

        root.addView(Theme.spacer(this,24))
        root.addView(Theme.row(this,"Choose blocked apps","${Prefs.blockedApps(this).size} selected"){
            startActivity(Intent(this,ManageAppsActivity::class.java))
        })
        scroll.addView(root);setContentView(scroll)
    }

    private fun duration(root:LinearLayout,minutes:Int){
        root.addView(Theme.row(this,"$minutes minutes","Start Focus Mode"){
            Prefs.setEnabled(this,true)
            Prefs.startFocus(this,minutes)
            Prefs.lockSettings(this)
            Toast.makeText(this,"Focus started",Toast.LENGTH_SHORT).show()
            render()
        })
        root.addView(Theme.spacer(this,10))
    }

    private fun stopFocus(){
        Prefs.stopFocus(this)
        Toast.makeText(this,"Focus stopped",Toast.LENGTH_SHORT).show()
        render()
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK) stopFocus()
    }

    companion object{private const val REQ_GATE=705}
}
