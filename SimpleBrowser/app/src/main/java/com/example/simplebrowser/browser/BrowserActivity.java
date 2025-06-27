package com.example.simplebrowser.browser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.simplebrowser.R;
import com.example.simplebrowser.history.HistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BrowserActivity extends BaseActivity {
    private static final String TAG = "BrowserActivity";
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        FloatingActionButton fabBackToMain = findViewById(R.id.fabBackToMain);

        setupWebView();
        setupSwipeRefresh();
        setupBottomNavigation();
        fabBackToMain.setOnClickListener(v -> returnToMainActivity());
        setupImageLongClick();
        overridePendingTransition(0, 0);

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            webView.loadUrl(url);
            Log.d(TAG, "Loading URL: " + url);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_back) {
                handleBackNavigation();
                return true;
            } else if (id == R.id.nav_home) {
                returnToMainActivity();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                // 预留设置页面入口
                return true;
            }
            return false;
        });
    }

    private void setupImageLongClick() {
        webView.setOnLongClickListener(v -> {
            WebView.HitTestResult result = webView.getHitTestResult();
            if (result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                String imageUrl = result.getExtra();
                showSaveImageDialog(imageUrl);
                return true;
            }
            return false;
        });
    }

    private void showSaveImageDialog(String imageUrl) {
        new AlertDialog.Builder(this)
                .setTitle("保存图片")
                .setMessage("是否要保存此图片？")
                .setPositiveButton("保存", (dialog, which) -> saveImage(imageUrl))
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveImage(String imageUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                "SimpleBrowser/" + System.currentTimeMillis() + ".jpg");

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "图片下载已开始", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setupWebView() {
        super.setupWebView();

        WebSettings webSettings = webView.getSettings();
        // 启用缩放支持
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false); // 隐藏原生缩放控件

        // 启用多点触控缩放
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // 设置缩放范围 (20%-500%)
        webSettings.setMinimumLogicalFontSize(8); // 对应最小20%缩放
        webSettings.setDefaultFontSize(40);       // 默认100%缩放
        webSettings.setDefaultFixedFontSize(40);  // 默认100%缩放

        // 设置文本缩放比例范围 (20%-500%)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webSettings.setTextZoom(100); // 默认100%
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Page started loading: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished loading: " + url);

                // 确保记录历史
                String title = view.getTitle();
                if (title == null || title.isEmpty()) {
                    title = "无标题";
                }
                addToHistory(title, url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (!isReload && url != null) {
                    String title = view.getTitle();
                    if (title == null || title.isEmpty()) {
                        title = "无标题";
                    }
                    addToHistory(title, url);
                }
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                // 限制缩放范围
                if (newScale < 0.2f) {  // 最小20%
                    webView.setInitialScale(20);
                } else if (newScale > 5.0f) {  // 最大500%
                    webView.setInitialScale(500);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "Page load error: " + error.getDescription());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                // 忽略SSL证书错误，包括证书过期、不信任等问题
                handler.proceed();
            }
        });
    }

    private void handleBackNavigation() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}
