package com.lixyz.lifekeeperforkotlin.view.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.LOAD_NO_CACHE
import androidx.appcompat.app.AppCompatActivity
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog


class PlanWebActivity : AppCompatActivity(), IPlanWebView {

    private var webView: WebView? = null

    private var waitDialog: CustomDialog? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // StatusBar 设置为透明
        window.statusBarColor = Color.TRANSPARENT
        window.setBackgroundDrawableResource(R.color.colorAccent)
        setContentView(R.layout.activity___plan_web)
        waitDialog = CustomDialog(this, this, "请稍候...")
        WebView.setWebContentsDebuggingEnabled(true)
        webView = findViewById(R.id.web_view)
        webView!!.setBackgroundColor(resources.getColor(R.color.PlanListActivityBackgroundColor, null))
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = LOAD_NO_CACHE
        val ws: WebSettings = webView!!.settings
        ws.allowUniversalAccessFromFileURLs = true
        val header: HashMap<String, String> = HashMap()
        val userId = getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
        if (userId != null) {
            header["Token"] = userId
        }

        deleteAllCache()
        webView!!.loadUrl("https://webview.li-xyz.com:1443", header)
        webView!!.addJavascriptInterface(this, "Android")
        webView!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event!!.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                        webView!!.goBack()
                        return true
                    }
                }
                return false
            }
        })
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: WebResourceRequest): Boolean {
                Log.d("TTT", "shouldOverrideUrlLoading: $url")
//                //判断url拦截事件
//                return if (url == "file:///android_asset/test2.html") {
//                    Log.e("TTT", "shouldOverrideUrlLoading: $url")
//                    startActivity(Intent(this@MainActivity, Main2Activity::class.java))
//                    true
//                } else {
//                    mWebView.loadUrl(url)
//                    false
//                }
                return true
            }
        }

        waitDialog!!.show()
    }

    @JavascriptInterface
    fun getUserId(): String? {
        return getSharedPreferences("LoginConfig", MODE_PRIVATE).getString("UserId", null)
    }

    private fun deleteAllCache() {
        cacheDir.delete()
        cacheDir.absoluteFile.delete()
        externalCacheDir?.delete()
        deleteDatabase("webview.db")
        deleteDatabase("webviewCache.db")
        WebView(this).clearCache(true)
    }

    @JavascriptInterface
    override fun showWaitDialog() {
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }

    @JavascriptInterface
    override fun hideWaitDialog() {
        if (waitDialog!!.isShowing) {
            waitDialog!!.dismiss()
        }
    }
}