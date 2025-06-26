package com.example.simplebrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity {
    protected WebView webView;
    protected SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkThemeChange();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkThemeChange();
    }

    protected void checkThemeChange() {
        if (isDarkModeEnabled()) {
            updateUiForDarkMode();
        } else {
            updateUiForLightMode();
        }
    }

    protected void updateUiForDarkMode() {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        if (webView != null) {
            webView.setBackgroundColor(ContextCompat.getColor(this, R.color.webViewBackgroundDark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                webView.getSettings().setForceDark(WebSettings.FORCE_DARK_ON);
            }
        }
    }

    protected void updateUiForLightMode() {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (webView != null) {
            webView.setBackgroundColor(ContextCompat.getColor(this, R.color.webViewBackground));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                webView.getSettings().setForceDark(WebSettings.FORCE_DARK_OFF);
            }
        }
    }

    protected boolean isDarkModeEnabled() {
        return (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    protected void setThemeTextColor(TextView textView, boolean isPrimary) {
        int colorId = isPrimary ? R.color.textColorPrimary : R.color.textColorSecondary;
        if (isDarkModeEnabled()) {
            colorId = isPrimary ? R.color.textColorPrimaryDark : R.color.textColorSecondaryDark;
        }
        textView.setTextColor(ContextCompat.getColor(this, colorId));
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void setupWebView() {
        if (webView == null) return;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        if (isDarkModeEnabled()) {
            updateUiForDarkMode();
        } else {
            updateUiForLightMode();
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
                if (isDarkModeEnabled()) {
                    injectDarkModeCSS(view);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                addToHistory(view.getTitle(), url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(BaseActivity.this, "加载页面出错", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
    }

    protected void injectDarkModeCSS(WebView webView) {
        String css = "body { color: white !important; background-color: #121212 !important; } " +
                "a { color: #BB86FC !important; }";
        String js = "var style = document.createElement('style'); style.innerHTML = '" + css + "'; document.head.appendChild(style);";
        webView.evaluateJavascript(js, null);
    }

    protected void addToHistory(String title, String url) {
        SharedPreferences prefs = getSharedPreferences("browser_history", MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(prefs.getString("history", "[]"));

            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("url").equals(url)) {
                    jsonArray.remove(i);
                    break;
                }
            }

            JSONObject newItem = new JSONObject();
            newItem.put("title", title != null ? title : "无标题");
            newItem.put("url", url);
            newItem.put("timestamp", System.currentTimeMillis());
            jsonArray.put(newItem);

            while (jsonArray.length() > 100) {
                jsonArray.remove(0);
            }

            prefs.edit().putString("history", jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void setupSwipeRefresh() {
        if (swipeRefreshLayout == null) return;

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (webView != null) {
                webView.reload();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    protected void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
