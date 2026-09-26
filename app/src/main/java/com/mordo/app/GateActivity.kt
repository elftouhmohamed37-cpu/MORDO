package com.mordo.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.security.SecureRandom

class GateActivity:Activity(){
    private var timer:CountDownTimer?=null
    private lateinit var root:LinearLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        Prefs.applyPendingUnlockIfDue(this)
        if(Prefs.settingsUnlocked(this)){ success(); return }
        root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(38,48,38,42)
            setBackgroundColor(Theme.BG)
        }
        setContentView(root)
        when(Prefs.mode(this)){
            Prefs.MODE_FRIEND->friendMode()
            Prefs.MODE_DELAY->delayMode()
            else->myselfMode()
        }
    }

    private fun heading(title:String,sub:String){
        root.addView(Theme.title(this,title))
        root.addView(Theme.sub(this,sub).apply{setPadding(0,8,0,24)})
    }

    private fun myselfMode(){
        heading("Myself Mode","Type the full random text correctly before time expires. Copy, paste and screenshots are disabled.")
        val phrase=generatePhrase(32)
        val timerText=TextView(this).apply{
            textSize=22f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Theme.RED); gravity=Gravity.CENTER
        }
        root.addView(timerText)
        root.addView(TextView(this).apply{
            text=phrase; textSize=18f; typeface=Typeface.MONOSPACE
            setTextColor(Theme.TEXT); setPadding(18,24,18,24); background=Theme.card()
        })
        root.addView(Theme.spacer(this,16))
        val input=NoPasteEditText(this).apply{
            hint="Type the text exactly"; textSize=17f; minLines=5; gravity=Gravity.TOP
            background=Theme.card(); setPadding(20,20,20,20)
        }
        root.addView(input,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Theme.spacer(this,14))
        root.addView(Theme.primaryButton(this,"Unlock settings"){
            if(input.text.toString()==phrase) success()
            else Toast.makeText(this,"The text is not an exact match",Toast.LENGTH_SHORT).show()
        })

        timer=object:CountDownTimer(45_000L,1000L){
            override fun onTick(ms:Long){ timerText.text="Time: ${(ms/1000L)+1}s" }
            override fun onFinish(){
                Toast.makeText(this@GateActivity,"Time expired. A new challenge is required.",Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED); finish()
            }
        }.start()
    }

    private fun friendMode(){
        heading("Friend Mode","Enter the PIN created for your accountability partner.")
        val input=EditText(this).apply{
            hint="PIN"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            textSize=20f; background=Theme.card(); setPadding(22,20,22,20)
        }
        root.addView(input)
        root.addView(Theme.spacer(this,18))
        root.addView(Theme.primaryButton(this,"Unlock"){
            if(Prefs.verifyFriendPin(this,input.text.toString())) success()
            else Toast.makeText(this,"Incorrect PIN",Toast.LENGTH_SHORT).show()
        })
    }

    private fun delayMode(){
        val at=Prefs.pendingUnlockAt(this)
        if(at>0 && System.currentTimeMillis()>=at){
            Prefs.applyPendingUnlockIfDue(this); success(); return
        }
        if(at>System.currentTimeMillis()){
            heading("Time Delay","Your unlock request is already waiting.")
            val remaining=((at-System.currentTimeMillis())/60000L+1).coerceAtLeast(1)
            root.addView(Theme.sectionTitle(this,"About $remaining minute(s) remaining"))
            root.addView(Theme.sub(this,"Close MORDO and return after the delay. Blocking stays active.").apply{setPadding(0,10,0,0)})
        }else{
            heading("Time Delay","Sensitive changes require a waiting period of ${Prefs.delayMinutes(this)} minutes.")
            root.addView(Theme.primaryButton(this,"Start unlock delay"){
                Prefs.scheduleDelayedUnlock(this)
                Toast.makeText(this,"Unlock delay started",Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED); finish()
            })
        }
    }

    private fun success(){
        Prefs.unlockSettings(this,5)
        setResult(RESULT_OK)
        finish()
    }

    override fun onDestroy(){ timer?.cancel(); super.onDestroy() }

    private fun generatePhrase(count:Int):String{
        val r=SecureRandom()
        return List(count){WORDS[r.nextInt(WORDS.size)]}.joinToString(" ")
    }

    companion object{
        private val WORDS=listOf(
            "anchor","apricot","atlas","bamboo","beacon","birch","cactus","cedar","cobalt","coral",
            "dawn","ember","falcon","fern","fjord","forest","galaxy","garden","harbor","hazel",
            "island","jade","juniper","lantern","lemon","maple","meadow","meteor","mint","mosaic",
            "mountain","nebula","oasis","ocean","olive","onyx","orchid","pebble","pine","planet",
            "quartz","raven","river","saffron","silver","spruce","stone","sunset","thunder","timber",
            "valley","violet","willow","winter","zenith","acorn","breeze","canyon","delta","frost"
        )
    }
}
