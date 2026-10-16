package com.mcyber12.aiflowbypasser

import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var toggleButton: Button
    private var desktopMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        toggleButton = Button(this).apply {
            text = "Desktop site"
        }

        val container = FrameLayout(this)
        container.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val buttonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 16
            rightMargin = 16
        }
        container.addView(toggleButton, buttonParams)

        val splash = TextView(this).apply {
            text = "Nova Web"
            textSize = 34f
            setTextColor(Color.parseColor("#A78BFA"))
            gravity = android.view.Gravity.CENTER
            background = ColorDrawable(Color.parseColor("#111827"))
        }
        container.addView(
            splash,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(container)

        splash.postDelayed({ splash.visibility = View.GONE }, 8000)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString = mobileUserAgent()
        try {
            WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, emptySet())
        } catch (e: Exception) {
            android.util.Log.e("Flow", "Requested-with allowlist failed", e)
        }

        WebView.setWebContentsDebuggingEnabled(true)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                inject(view)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                splash.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                splash.visibility = View.GONE
            }

        }
        webView.webChromeClient = WebChromeClient()

        toggleButton.setOnClickListener {
            desktopMode = !desktopMode
            settings.userAgentString = if (desktopMode) desktopUserAgent() else mobileUserAgent()
            toggleButton.text = if (desktopMode) "Mobile site" else "Desktop site"
            val current = webView.url ?: "https://flow.google.com/"
            webView.clearCache(true)
            webView.loadUrl(current)
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        webView.loadUrl("https://flow.google.com/")
    }

    private fun inject(view: WebView?) {
        try {
            val script = assets.open("flow_patch.js").bufferedReader().use { it.readText() }
            view?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            android.util.Log.e("Flow", "Failed to inject patch", e)
        }
    }

    private fun mobileUserAgent() =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private fun desktopUserAgent() =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}
