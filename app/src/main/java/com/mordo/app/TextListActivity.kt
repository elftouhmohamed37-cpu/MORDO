package com.mordo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class TextListActivity:Activity(){
    private var type="domains"
    private var pendingRemove:String?=null
    private lateinit var listBox:LinearLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        type=intent.getStringExtra("type")?:"domains"
        render()
    }

    private fun data():Set<String> = if(type=="domains") Prefs.domains(this) else Prefs.keywords(this)
    private fun save(v:Set<String>){ if(type=="domains") Prefs.setDomains(this,v) else Prefs.setKeywords(this,v) }

    private fun render(){
        val scroll=ScrollView(this).apply{setBackgroundColor(Theme.BG)}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(34,42,34,50)}
        root.addView(Theme.title(this,if(type=="domains")"Blocked websites" else "Blocked keywords"))
        root.addView(Theme.sub(this,if(type=="domains")
            "Add domains to block. Removing a domain requires your protection mode."
            else "Add words or phrases to block. Removing one requires your protection mode.").apply{setPadding(0,8,0,22)})

        val addRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val input=EditText(this).apply{
            hint=if(type=="domains")"example.com" else "word or phrase"
            singleLine=true; background=Theme.card(); setPadding(18,16,18,16)
        }
        addRow.addView(input,LinearLayout.LayoutParams(0,-2,1f))
        val add=Button(this).apply{
            text="Add"; isAllCaps=false
            setOnClickListener{
                val value=input.text.toString().trim().lowercase()
                if(value.isBlank()) return@setOnClickListener
                val cleaned=if(type=="domains") value.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/") else value
                save(data()+cleaned); input.setText(""); refreshList()
            }
        }
        addRow.addView(add)
        root.addView(addRow)
        root.addView(Theme.spacer(this,20))

        listBox=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        root.addView(listBox)
        scroll.addView(root); setContentView(scroll)
        refreshList()
    }

    private fun refreshList(){
        listBox.removeAllViews()
        data().sorted().forEach{item->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(20,14,10,14); background=Theme.card()
            }
            row.addView(TextView(this).apply{text=item;textSize=16f;setTextColor(Theme.TEXT)},LinearLayout.LayoutParams(0,-2,1f))
            row.addView(Button(this).apply{
                text="Remove";isAllCaps=false
                setOnClickListener{requestRemove(item)}
            })
            listBox.addView(row)
            listBox.addView(Theme.spacer(this,9))
        }
    }

    private fun requestRemove(item:String){
        if(Prefs.settingsUnlocked(this)){ remove(item); return }
        pendingRemove=item
        startActivityForResult(Intent(this,GateActivity::class.java),REQ_GATE)
    }

    private fun remove(item:String){
        save(data()-item); pendingRemove=null; refreshList()
        Toast.makeText(this,"Removed",Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==REQ_GATE && resultCode==RESULT_OK) pendingRemove?.let{remove(it)}
    }

    companion object{private const val REQ_GATE=701}
}
