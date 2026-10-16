package com.mcyber12.aiflowbypasser

import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.URLUtil
import android.net.Uri
import android.os.Environment
import android.app.DownloadManager as SystemDownloadManager
import androidx.webkit.WebSettingsCompat
import com.mcyber12.aiflowbypasser.downloads.DownloadManager
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var toggleButton: Button
    private lateinit var downloadsButton: Button
    private var desktopMode = false
    private var lastLoadedUrl = "https://flow.google.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        toggleButton = Button(this).apply {
            text = "Desktop site"
        }
        downloadsButton = Button(this).apply {
            text = "Downloads"
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

        val navParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 16
            leftMargin = 16
        }
        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val backButton = Button(this).apply { text = "Back" }
        val forwardButton = Button(this).apply { text = "Forward" }
        val reloadButton = Button(this).apply { text = "Reload" }
        val homeButton = Button(this).apply { text = "Home" }
        navRow.addView(backButton)
        navRow.addView(forwardButton)
        navRow.addView(reloadButton)
        navRow.addView(homeButton)
        container.addView(navRow, navParams)

        val downloadsParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            bottomMargin = 24
            rightMargin = 16
        }
        container.addView(downloadsButton, downloadsParams)

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
        backButton.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        forwardButton.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        reloadButton.setOnClickListener { webView.reload() }
        homeButton.setOnClickListener { lastLoadedUrl = "https://flow.google.com/"; webView.clearCache(true); webView.loadUrl(lastLoadedUrl) }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = SystemDownloadManager.Request(Uri.parse(url)).apply {
                addRequestHeader("User-Agent", userAgent)
                setTitle(fileName)
                setDescription("Downloading from Flow")
                setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager
            manager.enqueue(request)
            com.mcyber12.aiflowbypasser.downloads.DownloadManager.startDownload(fileName)
            Toast.makeText(this, "Download started: $fileName", Toast.LENGTH_SHORT).show()
        }

        toggleButton.setOnClickListener {
            desktopMode = !desktopMode
            settings.userAgentString = if (desktopMode) desktopUserAgent() else mobileUserAgent()
            toggleButton.text = if (desktopMode) "Mobile site" else "Desktop site"
            val current = lastLoadedUrl
            webView.clearCache(true)
            webView.loadUrl(current)
            lastLoadedUrl = current
        }

        downloadsButton.setOnClickListener {
            val downloads = DownloadManager.listDownloads()
            if (downloads.isEmpty()) {
                Toast.makeText(this, "No downloads yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val text = downloads.joinToString("\n") { download ->
                "${download.name}\n${if (download.downloaded) "${download.size} bytes" else "in progress"}"
            }
            AlertDialog.Builder(this)
                .setTitle("Downloads")
                .setMessage(text)
                .setPositiveButton("Close", null)
                .show()
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

        lastLoadedUrl = "https://flow.google.com/"
        webView.loadUrl(lastLoadedUrl)
    }

    private fun inject(view: WebView?) {
        for (asset in listOf("flow_patch.js", "theme.js")) {
            try {
                val script = assets.open(asset).bufferedReader().use { it.readText() }
                view?.evaluateJavascript(script, null)
            } catch (e: Exception) {
                android.util.Log.e("Flow", "Failed to inject $asset", e)
            }
        }
    }

    private fun mobileUserAgent() =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private fun desktopUserAgent() =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}
