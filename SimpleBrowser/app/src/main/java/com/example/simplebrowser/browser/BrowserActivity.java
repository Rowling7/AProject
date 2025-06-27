package com.example.simplebrowser.browser;

import android.os.Bundle;

import com.example.simplebrowser.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BrowserActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        FloatingActionButton fabBackToMain = findViewById(R.id.fabBackToMain);

        setupWebView();
        setupSwipeRefresh();

        fabBackToMain.setOnClickListener(v -> returnToMainActivity());

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            webView.loadUrl(url);
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
