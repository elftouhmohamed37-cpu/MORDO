package com.mordo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch

class ProtectionSettingsActivity:Activity(){
    private var pendingKey:String?=null
    private lateinit var root:LinearLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(34,42,34,50)}
        root.addView(Theme.title(this,"Content protection"))
        root.addView(Theme.sub(this,"Turning protection ON is immediate. Turning a category OFF requires your selected protection mode.").apply{setPadding(0,8,0,24)})

        addSwitch("Adult website blocking","Block known adult domains", "adult",Prefs.adultBlock(this))
        addSwitch("Keyword blocking","Block pages and searches containing your blocked words","keyword",Prefs.keywordBlock(this))
        addSwitch("App blocking","Block apps selected in Blocked Apps","app",Prefs.appBlock(this))
        addSwitch("YouTube protection","Block YouTube content matching your blocked keywords","youtube",Prefs.youtubeBlock(this))

        scroll.addView(root);setContentView(scroll)
    }

    private fun addSwitch(title:String,sub:String,key:String,checked:Boolean){
        val card=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;setPadding(24,20,24,20);background=Theme.card()
        }
        val sw=Switch(this).apply{
            text=title;isChecked=checked;textSize=17f
            setOnCheckedChangeListener{button:CompoundButton,value:Boolean->
                if(value){apply(key,true)}
                else if(Prefs.settingsUnlocked(this)) apply(key,false)
                else{
                    button.setOnCheckedChangeListener(null);button.isChecked=true
                    pendingKey=key
                    startActivityForResult(Intent(this@ProtectionSettingsActivity,GateActivity::class.java),REQ_GATE)
                }
            }
        }
        card.addView(sw);card.addView(Theme.sub(this@ProtectionSettingsActivity,sub).apply{setPadding(4,4,0,0)})
        root.addView(card);root.addView(Theme.spacer(this,12))
    }

    private fun apply(key:String,value:Boolean){
        when(key){
            "adult"->Prefs.setAdultBlock(this,value)
            "keyword"->Prefs.setKeywordBlock(this,value)
            "app"->Prefs.setAppBlock(this,value)
            "youtube"->Prefs.setYoutubeBlock(this,value)
        }
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK){
            pendingKey?.let{apply(it,false)}
            pendingKey=null
            render()
        }
    }

    companion object{private const val REQ_GATE=702}
}
