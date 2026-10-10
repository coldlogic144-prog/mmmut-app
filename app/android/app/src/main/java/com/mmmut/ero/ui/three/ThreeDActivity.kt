package com.mmmut.ero.ui.three

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.addCallback

/**
 * Full-screen host for the MMMUT Holographic Campus 3D frontend.
 *
 * The scene itself lives in `assets/campus3d/` and is plain HTML/CSS/WebGL, so
 * this activity deliberately does very little: it creates a WebView, loads the
 * bundled entry point, and forwards a few optional values as URL parameters.
 *
 * Why URL parameters instead of `evaluateJavascript` for the initial state:
 * file:// loads finish asynchronously, and a JS injection that lands before
 * `scene.js` has run is silently dropped. Query parameters are read by the page
 * during its own startup, so they cannot race. `evaluateJavascript` is still
 * exposed via [updateStats] for live updates after load.
 */
class ThreeDActivity : ComponentActivity() {

    private var web: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#05070F"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val view = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#05070F"))

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                // The page loads only from file:///android_asset; keep it that way.
                allowContentAccess = false
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                builtInZoomControls = false
                displayZoomControls = false
            }

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            loadUrl(buildEntryUrl())
        }

        root.addView(view)

        // System back should leave the scene rather than falling through to the
        // Compose NavHost, which has no idea this activity exists.
        onBackPressedDispatcher.addCallback(this) { finish() }

        setContentView(root)
        web = view
    }

    /** Composes `file:///android_asset/campus3d/index.html` plus any supplied state. */
    private fun buildEntryUrl(): String {
        val params = mutableListOf<String>()

        intent?.getStringExtra(EXTRA_ATTENDANCE)?.takeIf { it.isNotBlank() }
            ?.let { params.add("attendance=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_TERM)?.takeIf { it.isNotBlank() }
            ?.let { params.add("term=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_TERM_HINT)?.takeIf { it.isNotBlank() }
            ?.let { params.add("termHint=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_NEXT)?.takeIf { it.isNotBlank() }
            ?.let { params.add("next=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_NEXT_HINT)?.takeIf { it.isNotBlank() }
            ?.let { params.add("nextHint=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_SUBTITLE)?.takeIf { it.isNotBlank() }
            ?.let { params.add("subtitle=" + android.net.Uri.encode(it)) }
        intent?.getStringExtra(EXTRA_FOCUS)?.takeIf { it.isNotBlank() }
            ?.let { params.add("focus=" + android.net.Uri.encode(it)) }

        val base = "file:///android_asset/campus3d/index.html"
        return if (params.isEmpty()) base else base + "?" + params.joinToString("&")
    }

    /**
     * Pushes fresh values into the running scene. Safe to call any time after
     * the page reports ready; a no-op if the bridge is not present yet.
     */
    fun updateStats(attendance: String?, term: String?, next: String?) {
        val payload = buildString {
            append("window.MMMUT3D&&window.MMMUT3D.setStats({")
            attendance?.let { append("attendance:'").append(escape(it)).append("',") }
            term?.let { append("term:'").append(escape(it)).append("',") }
            next?.let { append("next:'").append(escape(it)).append("',") }
            append("})")
        }
        web?.evaluateJavascript(payload, null)
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")

    override fun onDestroy() {
        web?.let { v ->
            v.loadUrl("about:blank")
            v.destroy()
        }
        web = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ATTENDANCE = "extra_attendance"
        const val EXTRA_TERM = "extra_term"
        const val EXTRA_TERM_HINT = "extra_term_hint"
        const val EXTRA_NEXT = "extra_next"
        const val EXTRA_NEXT_HINT = "extra_next_hint"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_FOCUS = "extra_focus"
    }
}
