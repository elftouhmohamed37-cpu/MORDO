package com.mordo.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class ProtectionModeActivity:Activity(){
    private var pendingMode:String?=null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(34,42,34,50)}
        root.addView(Theme.title(this,"Protection mode"))
        root.addView(Theme.sub(this,"Choose what must happen before MORDO allows protective settings to be weakened or disabled.").apply{setPadding(0,8,0,24)})

        modeCard(root,"Myself Mode",
            "Type a long random 32-word challenge in 45 seconds. Copy, paste and screenshots are blocked.",
            Prefs.MODE_MYSELF)
        modeCard(root,"Friend Mode",
            "A trusted person chooses a PIN. Protective settings cannot be weakened without that PIN.",
            Prefs.MODE_FRIEND)
        modeCard(root,"Time Delay",
            "Request an unlock, then wait before protective settings can be changed.",
            Prefs.MODE_DELAY)

        root.addView(Theme.sub(this,"Current: "+currentLabel()).apply{setPadding(4,20,4,0)})
        scroll.addView(root);setContentView(scroll)
    }

    private fun modeCard(root:LinearLayout,title:String,sub:String,mode:String){
        val active=Prefs.mode(this)==mode
        val label=if(active)"✓  $title" else title
        root.addView(Theme.row(this,label,sub){requestMode(mode)})
        root.addView(Theme.spacer(this,12))
    }

    private fun requestMode(mode:String){
        if(mode==Prefs.mode(this)) return
        if(Prefs.settingsUnlocked(this)){configure(mode);return}
        pendingMode=mode
        startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
    }

    private fun configure(mode:String){
        when(mode){
            Prefs.MODE_FRIEND->configureFriend()
            Prefs.MODE_DELAY->configureDelay()
            else->{Prefs.setMode(this,Prefs.MODE_MYSELF);Prefs.lockSettings(this);render()}
        }
    }

    private fun configureFriend(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(30,8,30,0)}
        val pin=EditText(this).apply{
            hint="New PIN (4–10 digits)"
            inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        val confirm=EditText(this).apply{
            hint="Confirm PIN"
            inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        box.addView(pin);box.addView(confirm)
        AlertDialog.Builder(this)
            .setTitle("Set Friend PIN")
            .setMessage("Ask your accountability partner to enter and keep this PIN. Do not choose a PIN you already know if you want Friend Mode to resist impulses.")
            .setView(box)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Save"){_,_->
                val p=pin.text.toString()
                if(p.length in 4..10 && p==confirm.text.toString()){
                    Prefs.setFriendPin(this,p);Prefs.setMode(this,Prefs.MODE_FRIEND);Prefs.lockSettings(this);render()
                }else Toast.makeText(this,"PINs must match and contain 4–10 digits",Toast.LENGTH_LONG).show()
            }.show()
    }

    private fun configureDelay(){
        val input=EditText(this).apply{
            inputType=InputType.TYPE_CLASS_NUMBER
            setText(Prefs.delayMinutes(this@ProtectionModeActivity).toString())
            selectAll()
        }
        AlertDialog.Builder(this)
            .setTitle("Time Delay")
            .setMessage("How many minutes should MORDO wait before allowing protective settings to be weakened? (1–10080)")
            .setView(input)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Save"){_,_->
                val m=input.text.toString().toIntOrNull()
                if(m!=null && m in 1..10080){
                    Prefs.setDelayMinutes(this,m);Prefs.clearPendingUnlock(this)
                    Prefs.setMode(this,Prefs.MODE_DELAY);Prefs.lockSettings(this);render()
                }else Toast.makeText(this,"Enter a value from 1 to 10080",Toast.LENGTH_LONG).show()
            }.show()
    }

    private fun currentLabel()=when(Prefs.mode(this)){
        Prefs.MODE_FRIEND->"Friend Mode"
        Prefs.MODE_DELAY->"Time Delay (${Prefs.delayMinutes(this)} min)"
        else->"Myself Mode"
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK){
            pendingMode?.let{configure(it)}
            pendingMode=null
        }
    }

    companion object{private const val REQ_GATE=704}
}
