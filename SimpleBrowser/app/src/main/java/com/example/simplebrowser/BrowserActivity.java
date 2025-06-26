package com.example.simplebrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import com.example.simplebrowser.UserScript;

public class BrowserActivity extends AppCompatActivity {
    private WebView webView;
    private List<UserScript> userScripts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        webView = findViewById(R.id.webView);
        setupWebView();
        loadUserScripts();

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            webView.loadUrl(url);
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new ScriptInterface(), "AndroidScriptInterface");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAllScripts();
            }
        });
    }

    private void loadUserScripts() {
        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        String scriptsJson = prefs.getString("scripts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(scriptsJson);
            userScripts.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                userScripts.add(UserScript.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void injectAllScripts() {
        String currentUrl = webView.getUrl();
        if (currentUrl == null) return;

        for (UserScript script : userScripts) {
            if (script.isEnabled() && script.matchesUrl(currentUrl)) {
                injectScript(script.getCode());
            }
        }
    }

    private void injectScript(String script) {
        String wrappedScript = "(function(){" + script + "})();";
        webView.evaluateJavascript(wrappedScript, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class ScriptInterface {
        @JavascriptInterface
        public void showToast(String message) {
            // 可扩展更多JS与Android交互功能
        }
    }
}
