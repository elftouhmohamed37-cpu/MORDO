package com.mordo.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ManageAppsActivity:Activity(){
    private var pendingPackage:String?=null
    private data class AppItem(val label:String,val pkg:String)

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    private fun installedApps():List<AppItem>{
        val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent,PackageManager.MATCH_ALL)
            .map{
                val label=it.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                AppItem(if(label.isBlank())it.activityInfo.packageName else label,it.activityInfo.packageName)
            }
            .filter{it.pkg!=packageName}
            .distinctBy{it.pkg}
            .sortedBy{it.label.lowercase()}
    }

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(30,40,30,50)}
        root.addView(Theme.title(this,"Blocked apps"))
        root.addView(Theme.sub(this,"Select apps you want MORDO to block. Adding an app is immediate; removing one requires your protection mode.").apply{setPadding(0,8,0,22)})

        val selected=Prefs.blockedApps(this).toMutableSet()
        installedApps().forEach{app->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
                setPadding(18,14,14,14)
                background=Theme.card()
            }
            val icon=ImageView(this).apply{
                val d=runCatching{packageManager.getApplicationIcon(app.pkg)}.getOrNull()
                setImageDrawable(d)
            }
            row.addView(icon,LinearLayout.LayoutParams(64,64).apply{marginEnd=16})
            val texts=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
            texts.addView(TextView(this).apply{text=app.label;textSize=16f;setTextColor(Theme.TEXT)})
            texts.addView(Theme.sub(this,app.pkg))
            row.addView(texts,LinearLayout.LayoutParams(0,-2,1f))
            val cb=CheckBox(this).apply{
                isChecked=selected.contains(app.pkg)
                setOnCheckedChangeListener{button,value->
                    if(value){
                        selected.add(app.pkg);Prefs.setBlockedApps(this@ManageAppsActivity,selected)
                    }else if(Prefs.settingsUnlocked(this@ManageAppsActivity)){
                        selected.remove(app.pkg);Prefs.setBlockedApps(this@ManageAppsActivity,selected)
                    }else{
                        button.setOnCheckedChangeListener(null);button.isChecked=true
                        pendingPackage=app.pkg
                        startActivityForResult(Intent(this@ManageAppsActivity,GateActivity::class.java),REQ_GATE)
                    }
                }
            }
            row.addView(cb)
            root.addView(row);root.addView(Theme.spacer(this,9))
        }
        scroll.addView(root);setContentView(scroll)
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK){
            pendingPackage?.let{
                Prefs.setBlockedApps(this,Prefs.blockedApps(this)-it)
            }
            pendingPackage=null
            render()
        }
    }

    companion object{private const val REQ_GATE=703}
}
