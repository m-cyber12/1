package com.mcyber12.aiflowbypasser

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        WebView.setWebContentsDebuggingEnabled(true)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                inject(view)
            }
        }
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl("https://flow.google.com/")
    }

    private fun inject(view: WebView?) {
        try {
            val script = assets.open("flow_patch.js").bufferedReader().use { it.readText() }
            view?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            android.util.Log.e("FlowBypasser", "Failed to inject patch", e)
        }
    }
}
