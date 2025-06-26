package com.example.simplebrowser;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.SslError;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class BrowserActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        webView = findViewById(R.id.webView);
        setupWebView();

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            webView.loadUrl(url);
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();

        // 基础设置
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // 缓存设置
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 自适应设置
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // 混合内容处理 (HTTP+HTTPS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置自定义WebViewClient
        webView.setWebViewClient(new CustomWebViewClient());

        // 设置WebChromeClient以处理进度条等
        webView.setWebChromeClient(new WebChromeClient());
    }

    /**
     * 自定义WebViewClient以处理SSL错误和其他页面控制
     */
    private class CustomWebViewClient extends WebViewClient {
        // 处理SSL错误（包括证书过期、无效等）
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 忽略所有SSL错误，继续加载
            handler.proceed();

            // 如果需要可以根据错误类型定制处理
            /*
            switch(error.getPrimaryError()) {
                case SslError.SSL_EXPIRED:
                case SslError.SSL_IDMISMATCH:
                case SslError.SSL_NOTYETVALID:
                case SslError.SSL_UNTRUSTED:
                    handler.proceed();
                    break;
                default:
                    handler.cancel();
            }
            */
        }

        // 处理页面加载
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        // 处理API 24+的页面加载
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
