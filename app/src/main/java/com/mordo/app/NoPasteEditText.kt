package com.mordo.app

import android.content.Context
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

class NoPasteEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null): EditText(context,attrs) {
    private val disabled=object: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?)=false
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?)=false
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?)=false
        override fun onDestroyActionMode(mode: ActionMode?)=Unit
    }
    init { customSelectionActionModeCallback=disabled; setTextIsSelectable(false); isLongClickable=false }
    override fun onTextContextMenuItem(id:Int)=false
}