package com.mordo.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.security.SecureRandom

class MyselfActivity: Activity() {
    private lateinit var phrase:String
    private lateinit var timerText:TextView
    private lateinit var input:NoPasteEditText
    private lateinit var confirm:Button
    private var secondsLeft=CHALLENGE_SECONDS
    private var timer:CountDownTimer?=null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        phrase=generatePhrase(WORD_COUNT)
        buildUi(); startTimer()
    }

    private fun buildUi() {
        val root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(40,48,40,40)
        }
        root.addView(TextView(this).apply {
            text="Myself Mode"; textSize=30f; typeface=Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text="Type the text exactly before time expires. Paste and screenshots are disabled. Success starts a 15-minute cooling-off delay; blocking remains active during that time."
            textSize=16f; setPadding(0,20,0,20)
        })
        timerText=TextView(this).apply {
            textSize=22f; gravity=Gravity.CENTER; typeface=Typeface.MONOSPACE
        }
        root.addView(timerText)
        root.addView(TextView(this).apply {
            text=phrase; textSize=18f; typeface=Typeface.MONOSPACE
            setPadding(16,24,16,24)
        })
        input=NoPasteEditText(this).apply {
            hint="Type here"; textSize=18f; isSingleLine=false; minLines=4
        }
        confirm=Button(this).apply {
            text="Confirm"; isEnabled=false; setOnClickListener { verify() }
        }
        val cancel=Button(this).apply {
            text="Keep protection on"; setOnClickListener { setResult(RESULT_CANCELED); finish() }
        }

        input.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int)=Unit
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) {
                confirm.isEnabled=s?.toString()==phrase
            }
            override fun afterTextChanged(s:Editable?)=Unit
        })

        root.addView(input,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(confirm,LinearLayout.LayoutParams(-1,-2))
        root.addView(cancel,LinearLayout.LayoutParams(-1,-2))
        setContentView(root)
    }

    private fun startTimer() {
        timer?.cancel()
        timer=object:CountDownTimer(CHALLENGE_SECONDS*1000L,1000L) {
            override fun onTick(ms:Long) {
                secondsLeft=(ms/1000L).toInt()+1
                timerText.text="Time: ${secondsLeft}s"
            }
            override fun onFinish() { setResult(RESULT_CANCELED); finish() }
        }.start()
    }

    private fun verify() {
        if(secondsLeft>0 && input.text.toString()==phrase) {
            Prefs.scheduleUnlock(this,System.currentTimeMillis()+COOL_OFF_MILLIS)
            setResult(RESULT_OK); finish()
        }
    }

    override fun onDestroy() { timer?.cancel(); super.onDestroy() }

    private fun generatePhrase(count:Int):String {
        val random=SecureRandom()
        return List(count) { WORDS[random.nextInt(WORDS.size)] }.joinToString(" ")
    }

    companion object {
        private const val WORD_COUNT=24
        private const val CHALLENGE_SECONDS=50
        private const val COOL_OFF_MILLIS=15*60*1000L
        private val WORDS=listOf(
            "anchor","apricot","atlas","bamboo","beacon","birch","cactus","cedar",
            "cobalt","coral","dawn","ember","falcon","fern","fjord","forest",
            "galaxy","garden","harbor","hazel","island","jade","juniper","lantern",
            "lemon","maple","meadow","meteor","mint","mosaic","mountain","nebula",
            "oasis","ocean","olive","onyx","orchid","pebble","pine","planet",
            "quartz","raven","river","saffron","silver","spruce","stone","sunset",
            "thunder","timber","valley","violet","willow","winter","zenith"
        )
    }
}
