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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var pending: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buildUi() {
        val scroll=ScrollView(this)
        val root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(40,48,40,48)
        }

        root.addView(TextView(this).apply {
            text="MORDO"; textSize=38f; typeface=Typeface.DEFAULT_BOLD
            gravity=Gravity.CENTER_HORIZONTAL
        })
        root.addView(TextView(this).apply {
            text="Free self-control blocker — test build 0.2"
            textSize=16f; gravity=Gravity.CENTER_HORIZONTAL
            setPadding(0,8,0,32)
        })

        status=TextView(this).apply { textSize=19f }
        pending=TextView(this).apply { textSize=16f; setPadding(0,8,0,24) }
        root.addView(status); root.addView(pending)

        root.addView(button("Enable blocking service") {
            AlertDialog.Builder(this)
                .setTitle("Accessibility permission")
                .setMessage("MORDO needs Accessibility access to read the current app name and visible on-screen text so it can detect blocked websites, keywords and apps and cover them with a blocking screen. Processing stays on your device. MORDO does not use this permission to perform clicks or actions for you.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Continue") { _,_ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                .show()
        })

        root.addView(button("Add removal friction (Device Admin)") {
            val admin=ComponentName(this,MordoDeviceAdminReceiver::class.java)
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "MORDO uses Device Admin only to add an extra deliberate step before removal. It cannot make uninstall impossible.")
            })
        })

        root.addView(button("Disable protection — Myself Mode") {
            when {
                !Prefs.isEnabled(this) -> Toast.makeText(this,"Protection is already off",Toast.LENGTH_SHORT).show()
                Prefs.pendingUnlockAt(this)>System.currentTimeMillis() -> Toast.makeText(this,"An unlock delay is already running",Toast.LENGTH_SHORT).show()
                else -> startActivityForResult(Intent(this,MyselfActivity::class.java),REQ_MYSELF)
            }
        })

        root.addView(button("Cancel pending unlock") {
            Prefs.clearPendingUnlock(this); Prefs.setEnabled(this,true); refresh()
        })
        root.addView(button("Turn protection back ON") {
            Prefs.clearPendingUnlock(this); Prefs.setEnabled(this,true); refresh()
        })

        root.addView(TextView(this).apply {
            text="This is the first installable test build. The next builds add list editing, app selection, Focus Mode, Safe Search controls and additional protection modes."
            textSize=15f; setPadding(0,28,0,0)
        })

        scroll.addView(root); setContentView(scroll)
    }

    private fun button(label:String,action:()->Unit)=Button(this).apply {
        text=label; textSize=16f; setOnClickListener { action() }
    }

    private fun refresh() {
        Prefs.applyPendingUnlockIfDue(this)
        status.text=if(Prefs.isEnabled(this)) "Protection: ON" else "Protection: OFF"
        val at=Prefs.pendingUnlockAt(this)
        pending.text=if(at>System.currentTimeMillis()) {
            val min=((at-System.currentTimeMillis())/60000L)+1
            "Pending unlock: about $min minute(s) remaining. Blocking stays ON until then."
        } else "No pending unlock."
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_MYSELF) {
            refresh()
            if(resultCode==RESULT_OK) Toast.makeText(this,"Challenge passed. 15-minute delay started.",Toast.LENGTH_LONG).show()
        }
    }

    companion object { private const val REQ_MYSELF=1001 }
}
