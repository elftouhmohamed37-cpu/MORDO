package com.mordo.blocker

import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque

object NodeTextExtractor {
    fun extract(root: AccessibilityNodeInfo?, maxNodes: Int = 700): String {
        if (root == null) return ""
        val out = StringBuilder()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var count = 0
        while (queue.isNotEmpty() && count < maxNodes) {
            val node = queue.removeFirst(); count++
            node.text?.let { out.append(it).append('\n') }
            node.contentDescription?.let { out.append(it).append('\n') }
            node.viewIdResourceName?.let { out.append(it).append('\n') }
            for (i in 0 until node.childCount) node.getChild(i)?.let(queue::addLast)
        }
        return out.toString()
    }
}
