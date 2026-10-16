package com.mcyber12.aiflowbypasser

import android.app.AlertDialog
import android.app.DownloadManager as SystemDownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import com.mcyber12.aiflowbypasser.downloads.DownloadManager

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var splash: View
    private lateinit var desktopButton: ImageButton
    private var desktopMode = false
    private val homeUrl = "https://flow.google.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val forwardButton = findViewById<ImageButton>(R.id.forwardButton)
        val reloadButton = findViewById<ImageButton>(R.id.reloadButton)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        desktopButton = findViewById(R.id.desktopButton)
        val downloadsButton = findViewById<ImageButton>(R.id.downloadsButton)
        webView = findViewById(R.id.webView)
        splash = findViewById(R.id.splash)

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
        homeButton.setOnClickListener {
            webView.clearCache(true)
            webView.loadUrl(homeUrl)
        }

        desktopButton.setOnClickListener {
            desktopMode = !desktopMode
            settings.userAgentString = if (desktopMode) desktopUserAgent() else mobileUserAgent()
            desktopButton.alpha = if (desktopMode) 1f else 0.5f
            webView.clearCache(true)
            val current = if (webView.url.isNullOrBlank()) homeUrl else webView.url
            webView.loadUrl(current)
        }
        desktopButton.alpha = 0.5f

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = SystemDownloadManager.Request(Uri.parse(url)).apply {
                addRequestHeader("User-Agent", userAgent)
                setTitle(fileName)
                setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager
            manager.enqueue(request)
            DownloadManager.startDownload(fileName)
            Toast.makeText(this, "Download started: $fileName", Toast.LENGTH_SHORT).show()
        }

        downloadsButton.setOnClickListener {
            showDownloads()
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

        splash.postDelayed({ splash.visibility = View.GONE }, 8000)
        webView.loadUrl(homeUrl)
    }

    private fun showDownloads() {
        val downloads = DownloadManager.listDownloads()
        if (downloads.isEmpty()) {
            Toast.makeText(this, "No downloads yet", Toast.LENGTH_SHORT).show()
            return
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
