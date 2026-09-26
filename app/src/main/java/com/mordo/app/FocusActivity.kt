package com.mordo.app

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class FocusActivity:Activity(){
    private var pendingAction:String?=null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume(){super.onResume();render()}

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(34,42,34,50)}
        root.addView(Theme.title(this,"Focus Mode"))
        root.addView(Theme.sub(this,
            "When Focus is active, only calls/SMS, Android essentials and the apps you explicitly allow can open. All other apps are blocked.").apply{setPadding(0,8,0,24)})

        if(Prefs.focusActive(this)){
            root.addView(Theme.row(this,"Focus is ACTIVE",focusStatus()){ })
            root.addView(Theme.spacer(this,12))
            root.addView(Theme.primaryButton(this,"Stop active Focus early"){
                requestSensitive("stop")
            })
            root.addView(Theme.spacer(this,18))
        }

        root.addView(Theme.sectionTitle(this,"Quick focus"))
        root.addView(Theme.sub(this,"Start immediately for a fixed duration.").apply{setPadding(0,4,0,12)})
        duration(root,25);duration(root,45);duration(root,60);duration(root,120);duration(root,240)

        root.addView(Theme.spacer(this,20))
        root.addView(Theme.sectionTitle(this,"Daily schedule"))
        root.addView(Theme.sub(this,
            "Current: ${if(Prefs.focusScheduleEnabled(this))"ON" else "OFF"} · ${Prefs.formatMinutes(Prefs.focusStartMinutes(this))} → ${Prefs.formatMinutes(Prefs.focusEndMinutes(this))}"
        ).apply{setPadding(0,4,0,12)})

        root.addView(Theme.row(this,"Start time",Prefs.formatMinutes(Prefs.focusStartMinutes(this))){
            chooseTime(true)
        })
        root.addView(Theme.spacer(this,10))
        root.addView(Theme.row(this,"End time",Prefs.formatMinutes(Prefs.focusEndMinutes(this))){
            chooseTime(false)
        })
        root.addView(Theme.spacer(this,10))

        if(Prefs.focusScheduleEnabled(this)){
            root.addView(Theme.primaryButton(this,"Disable daily schedule"){requestSensitive("disable_schedule")})
        }else{
            root.addView(Theme.primaryButton(this,"Enable daily schedule"){
                Prefs.setFocusScheduleEnabled(this,true);Prefs.setEnabled(this,true);Prefs.lockSettings(this);render()
            })
        }

        root.addView(Theme.spacer(this,24))
        root.addView(Theme.row(this,"Apps allowed during Focus","${Prefs.focusAllowedApps(this).size} custom app(s) allowed"){
            startActivity(Intent(this,FocusAllowedAppsActivity::class.java))
        })

        scroll.addView(root);setContentView(scroll)
    }

    private fun duration(root:LinearLayout,minutes:Int){
        root.addView(Theme.row(this,"$minutes minutes","Start now"){
            Prefs.setEnabled(this,true)
            Prefs.startFocus(this,minutes)
            Prefs.lockSettings(this)
            Toast.makeText(this,"Focus started",Toast.LENGTH_SHORT).show()
            render()
        })
        root.addView(Theme.spacer(this,9))
    }

    private fun chooseTime(start:Boolean){
        val current=if(start)Prefs.focusStartMinutes(this) else Prefs.focusEndMinutes(this)
        TimePickerDialog(this,{_,hour,minute->
            if(Prefs.settingsUnlocked(this)){
                saveTime(start,hour*60+minute)
            }else{
                pendingAction=(if(start)"start:" else "end:")+(hour*60+minute)
                startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
            }
        },current/60,current%60,true).show()
    }

    private fun saveTime(start:Boolean,value:Int){
        val s=if(start)value else Prefs.focusStartMinutes(this)
        val e=if(start)Prefs.focusEndMinutes(this) else value
        Prefs.setFocusTimes(this,s,e);render()
    }

    private fun requestSensitive(action:String){
        if(Prefs.settingsUnlocked(this)){complete(action);return}
        pendingAction=action
        startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
    }

    private fun complete(action:String){
        when{
            action=="stop"->Prefs.stopFocus(this)
            action=="disable_schedule"->Prefs.setFocusScheduleEnabled(this,false)
            action.startsWith("start:")->saveTime(true,action.substringAfter(":").toInt())
            action.startsWith("end:")->saveTime(false,action.substringAfter(":").toInt())
        }
        render()
    }

    private fun focusStatus():String{
        val until=Prefs.focusUntil(this)
        if(until>System.currentTimeMillis()){
            val mins=((until-System.currentTimeMillis())/60000L+1).coerceAtLeast(1)
            return "Manual session · about $mins min remaining"
        }
        return "Daily schedule ${Prefs.formatMinutes(Prefs.focusStartMinutes(this))} → ${Prefs.formatMinutes(Prefs.focusEndMinutes(this))}"
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK){
            pendingAction?.let{complete(it)}
            pendingAction=null
        }
    }

    companion object{private const val REQ_GATE=705}
}
