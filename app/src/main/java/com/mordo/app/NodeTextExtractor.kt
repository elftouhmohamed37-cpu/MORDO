package com.mordo.app

import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque

object NodeTextExtractor {
    fun extract(root: AccessibilityNodeInfo?, maxNodes:Int=500):String {
        if (root==null) return ""
        val out=StringBuilder(); val q=ArrayDeque<AccessibilityNodeInfo>(); q.add(root)
        var count=0
        while(q.isNotEmpty() && count<maxNodes) {
            val n=q.removeFirst(); count++
            n.text?.let { out.append(it).append('\n') }
            n.contentDescription?.let { out.append(it).append('\n') }
            n.viewIdResourceName?.let { out.append(it).append('\n') }
            for(i in 0 until n.childCount) n.getChild(i)?.let(q::addLast)
        }
        return out.toString()
    }
}