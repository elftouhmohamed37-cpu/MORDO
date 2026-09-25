package com.mordo.app

import android.content.Context

object Prefs {
    private const val NAME="mordo_prefs"
    private const val ENABLED="enabled"
    private const val DOMAINS="domains"
    private const val KEYWORDS="keywords"
    private const val APPS="apps"
    private const val PENDING="pending_unlock_at"

    private val defaultDomains=setOf("pornhub.com","xvideos.com","xnxx.com","redtube.com","youporn.com","xhamster.com","spankbang.com","rule34.xxx")
    private val defaultKeywords=setOf("porn","porno","pornography","xxx","hentai","rule34","sex video","adult video","nude video","nudes")
    private fun p(c:Context)=c.getSharedPreferences(NAME,Context.MODE_PRIVATE)

    fun isEnabled(c:Context)=p(c).getBoolean(ENABLED,true)
    fun setEnabled(c:Context,v:Boolean)=p(c).edit().putBoolean(ENABLED,v).apply()
    fun domains(c:Context)=p(c).getStringSet(DOMAINS,null)?.toSet()?:defaultDomains
    fun keywords(c:Context)=p(c).getStringSet(KEYWORDS,null)?.toSet()?:defaultKeywords
    fun blockedApps(c:Context)=p(c).getStringSet(APPS,emptySet())?.toSet()?:emptySet()
    fun pendingUnlockAt(c:Context)=p(c).getLong(PENDING,0L)
    fun scheduleUnlock(c:Context,t:Long)=p(c).edit().putLong(PENDING,t).apply()
    fun clearPendingUnlock(c:Context)=p(c).edit().remove(PENDING).apply()
    fun applyPendingUnlockIfDue(c:Context):Boolean {
        val t=pendingUnlockAt(c)
        if(t>0 && System.currentTimeMillis()>=t) { setEnabled(c,false); clearPendingUnlock(c); return true }
        return false
    }
}