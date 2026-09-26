package com.mordo.app

import android.content.Context
import java.security.MessageDigest
import java.util.Calendar

object Prefs {
    private const val NAME="mordo_prefs"
    private const val ENABLED="enabled"
    private const val DOMAINS="domains"
    private const val KEYWORDS="keywords"
    private const val APPS="apps"
    private const val ADULT_BLOCK="adult_block"
    private const val KEYWORD_BLOCK="keyword_block"
    private const val APP_BLOCK="app_block"
    private const val YOUTUBE_BLOCK="youtube_block"
    private const val MODE="protection_mode"
    private const val FRIEND_PIN="friend_pin_hash"
    private const val DELAY_MINUTES="delay_minutes"
    private const val SETTINGS_UNLOCK_UNTIL="settings_unlock_until"
    private const val PENDING_UNLOCK_AT="pending_unlock_at"
    private const val FOCUS_UNTIL="focus_until"
    private const val FOCUS_ALLOWED="focus_allowed"
    private const val FOCUS_SCHEDULE="focus_schedule"
    private const val FOCUS_START="focus_start"
    private const val FOCUS_END="focus_end"
    private const val SYSTEM_SETUP_UNTIL="system_setup_until"

    const val MODE_MYSELF="MYSELF"
    const val MODE_FRIEND="FRIEND"
    const val MODE_DELAY="DELAY"

    private val defaultDomains=setOf(
        "pornhub.com","xvideos.com","xnxx.com","xhamster.com","redtube.com",
        "youporn.com","spankbang.com","tube8.com","beeg.com","tnaflix.com",
        "drtuber.com","porn.com","pornone.com","hclips.com","eporner.com",
        "porndig.com","youjizz.com","hqporner.com","4tube.com","sunporno.com",
        "rule34.xxx","nhentai.net","hanime.tv","hentaihaven.xxx"
    )

    private val defaultKeywords=setOf(
        "porn","porno","pornography","xxx video","hentai","rule34",
        "sex video","adult video","nude video","nudes","onlyfans nude",
        "pornstar","hardcore porn","free porn"
    )

    private fun p(c:Context)=c.getSharedPreferences(NAME,Context.MODE_PRIVATE)

    fun isEnabled(c:Context)=p(c).getBoolean(ENABLED,true)
    fun setEnabled(c:Context,v:Boolean)=p(c).edit().putBoolean(ENABLED,v).apply()

    fun adultBlock(c:Context)=p(c).getBoolean(ADULT_BLOCK,true)
    fun setAdultBlock(c:Context,v:Boolean)=p(c).edit().putBoolean(ADULT_BLOCK,v).apply()

    fun keywordBlock(c:Context)=p(c).getBoolean(KEYWORD_BLOCK,true)
    fun setKeywordBlock(c:Context,v:Boolean)=p(c).edit().putBoolean(KEYWORD_BLOCK,v).apply()

    fun appBlock(c:Context)=p(c).getBoolean(APP_BLOCK,true)
    fun setAppBlock(c:Context,v:Boolean)=p(c).edit().putBoolean(APP_BLOCK,v).apply()

    fun youtubeBlock(c:Context)=p(c).getBoolean(YOUTUBE_BLOCK,true)
    fun setYoutubeBlock(c:Context,v:Boolean)=p(c).edit().putBoolean(YOUTUBE_BLOCK,v).apply()

    fun domains(c:Context)=p(c).getStringSet(DOMAINS,null)?.toSet()?:defaultDomains
    fun setDomains(c:Context,v:Set<String>)=p(c).edit().putStringSet(DOMAINS,v).apply()

    fun keywords(c:Context)=p(c).getStringSet(KEYWORDS,null)?.toSet()?:defaultKeywords
    fun setKeywords(c:Context,v:Set<String>)=p(c).edit().putStringSet(KEYWORDS,v).apply()

    fun blockedApps(c:Context)=p(c).getStringSet(APPS,emptySet())?.toSet()?:emptySet()
    fun setBlockedApps(c:Context,v:Set<String>)=p(c).edit().putStringSet(APPS,v).apply()

    fun mode(c:Context)=p(c).getString(MODE,MODE_MYSELF)?:MODE_MYSELF
    fun setMode(c:Context,v:String)=p(c).edit().putString(MODE,v).apply()

    fun delayMinutes(c:Context)=p(c).getInt(DELAY_MINUTES,30)
    fun setDelayMinutes(c:Context,v:Int)=p(c).edit().putInt(DELAY_MINUTES,v.coerceIn(1,10080)).apply()

    fun setFriendPin(c:Context,pin:String)=p(c).edit().putString(FRIEND_PIN,hash(pin)).apply()
    fun hasFriendPin(c:Context)=!p(c).getString(FRIEND_PIN,null).isNullOrBlank()
    fun verifyFriendPin(c:Context,pin:String)=p(c).getString(FRIEND_PIN,"")==hash(pin)

    fun settingsUnlocked(c:Context)=System.currentTimeMillis()<p(c).getLong(SETTINGS_UNLOCK_UNTIL,0L)
    fun beginSystemSetup(c:Context,minutes:Int=3)=p(c).edit().putLong(SYSTEM_SETUP_UNTIL,System.currentTimeMillis()+minutes*60_000L).apply()
    fun systemSetupActive(c:Context)=System.currentTimeMillis()<p(c).getLong(SYSTEM_SETUP_UNTIL,0L)
    fun unlockSettings(c:Context,minutes:Int=5)=p(c).edit().putLong(SETTINGS_UNLOCK_UNTIL,System.currentTimeMillis()+minutes*60_000L).apply()
    fun lockSettings(c:Context)=p(c).edit().remove(SETTINGS_UNLOCK_UNTIL).apply()

    fun pendingUnlockAt(c:Context)=p(c).getLong(PENDING_UNLOCK_AT,0L)
    fun scheduleDelayedUnlock(c:Context)=p(c).edit().putLong(PENDING_UNLOCK_AT,System.currentTimeMillis()+delayMinutes(c)*60_000L).apply()
    fun clearPendingUnlock(c:Context)=p(c).edit().remove(PENDING_UNLOCK_AT).apply()
    fun applyPendingUnlockIfDue(c:Context):Boolean{
        val at=pendingUnlockAt(c)
        if(at>0 && System.currentTimeMillis()>=at){
            unlockSettings(c)
            clearPendingUnlock(c)
            return true
        }
        return false
    }

    fun focusUntil(c:Context)=p(c).getLong(FOCUS_UNTIL,0L)
    fun startFocus(c:Context,minutes:Int)=p(c).edit().putLong(FOCUS_UNTIL,System.currentTimeMillis()+minutes*60_000L).apply()
    fun stopFocus(c:Context)=p(c).edit().remove(FOCUS_UNTIL).apply()

    fun focusAllowedApps(c:Context)=p(c).getStringSet(FOCUS_ALLOWED,emptySet())?.toSet()?:emptySet()
    fun setFocusAllowedApps(c:Context,v:Set<String>)=p(c).edit().putStringSet(FOCUS_ALLOWED,v).apply()

    fun focusScheduleEnabled(c:Context)=p(c).getBoolean(FOCUS_SCHEDULE,false)
    fun setFocusScheduleEnabled(c:Context,v:Boolean)=p(c).edit().putBoolean(FOCUS_SCHEDULE,v).apply()
    fun focusStartMinutes(c:Context)=p(c).getInt(FOCUS_START,22*60+30)
    fun focusEndMinutes(c:Context)=p(c).getInt(FOCUS_END,6*60)
    fun setFocusTimes(c:Context,start:Int,end:Int)=p(c).edit()
        .putInt(FOCUS_START,start.coerceIn(0,1439))
        .putInt(FOCUS_END,end.coerceIn(0,1439)).apply()

    fun focusActive(c:Context):Boolean{
        if(focusUntil(c)>System.currentTimeMillis()) return true
        if(!focusScheduleEnabled(c)) return false
        val cal=Calendar.getInstance()
        val now=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE)
        val start=focusStartMinutes(c)
        val end=focusEndMinutes(c)
        return if(start==end) true
        else if(start<end) now in start until end
        else now>=start || now<end
    }

    fun formatMinutes(v:Int):String{
        val h=(v/60)%24
        val m=v%60
        return String.format("%02d:%02d",h,m)
    }

    private fun hash(s:String):String{
        val bytes=MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        return bytes.joinToString(""){"%02x".format(it)}
    }
}
