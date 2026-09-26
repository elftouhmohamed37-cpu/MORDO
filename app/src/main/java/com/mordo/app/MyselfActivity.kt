package com.mordo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Kept only for compatibility with earlier test builds.
 * The real protection challenge now lives in GateActivity.
 */
class MyselfActivity:Activity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        startActivityForResult(Intent(this,GateActivity::class.java),901)
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        setResult(resultCode)
        finish()
    }
}
