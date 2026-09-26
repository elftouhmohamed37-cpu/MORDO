package com.mordo.blocker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        web = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            if (android.os.Build.VERSION.SDK_INT >= 26) settings.safeBrowsingEnabled = true
            addJavascriptInterface(NativeBridge(this@MainActivity), "MordoNative")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        return true
                    }
                    return false
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val uri = url?.let(Uri::parse) ?: return false
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        return true
                    }
                    return false
                }
            }
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(web)
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) web.evaluateJavascript("if(window.render){render();}", null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!::web.isInitialized) { super.onBackPressed(); return }
        web.evaluateJavascript("(function(){return window.mordoBack?window.mordoBack():false;})()") { result ->
            if (result != "true") finish()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.removeJavascriptInterface("MordoNative")
            web.destroy()
        }
        super.onDestroy()
    }
}
