package com.mordo.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Theme {
    const val BG=0xFFF7F7F9.toInt()
    const val CARD=0xFFFFFFFF.toInt()
    const val TEXT=0xFF17171B.toInt()
    const val SUB=0xFF6F7078.toInt()
    const val DARK=0xFF17171B.toInt()
    const val GREEN=0xFF15803D.toInt()
    const val RED=0xFFB42318.toInt()

    fun card():GradientDrawable=GradientDrawable().apply {
        setColor(CARD); cornerRadius=28f
        setStroke(1,0xFFE5E5EA.toInt())
    }

    fun primaryButton(c:Context,label:String,onClick:()->Unit)=Button(c).apply {
        text=label; isAllCaps=false; textSize=16f
        setTextColor(Color.WHITE)
        background=GradientDrawable().apply { setColor(DARK); cornerRadius=24f }
        setOnClickListener { onClick() }
        setPadding(20,18,20,18)
    }

    fun title(c:Context,textValue:String)=TextView(c).apply {
        text=textValue; textSize=28f; setTextColor(TEXT); typeface=Typeface.DEFAULT_BOLD
    }

    fun sectionTitle(c:Context,textValue:String)=TextView(c).apply {
        text=textValue; textSize=18f; setTextColor(TEXT); typeface=Typeface.DEFAULT_BOLD
    }

    fun sub(c:Context,textValue:String)=TextView(c).apply {
        text=textValue; textSize=14f; setTextColor(SUB)
    }

    fun row(c:Context,title:String,subtitle:String,onClick:()->Unit):View {
        return LinearLayout(c).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(28,24,28,24)
            background=card()
            addView(sectionTitle(c,title))
            addView(sub(c,subtitle).apply { setPadding(0,6,0,0) })
            setOnClickListener { onClick() }
        }
    }

    fun spacer(c:Context,h:Int)=View(c).apply { layoutParams=LinearLayout.LayoutParams(1,h) }

    fun statusPill(c:Context,on:Boolean):TextView=TextView(c).apply {
        text=if(on) "PROTECTION ON" else "PROTECTION OFF"
        textSize=13f; typeface=Typeface.DEFAULT_BOLD
        setTextColor(if(on) GREEN else RED)
        gravity=Gravity.CENTER
        setPadding(18,10,18,10)
        background=GradientDrawable().apply {
            setColor(if(on) 0xFFEAF7EF.toInt() else 0xFFFDECEC.toInt())
            cornerRadius=40f
        }
    }
}
