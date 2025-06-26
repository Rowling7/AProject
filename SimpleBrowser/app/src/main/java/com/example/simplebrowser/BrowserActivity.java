package com.example.simplebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BrowserActivity extends BaseActivity {
    // 添加常量定义
    private static final int HISTORY_REQUEST_CODE = 1001;

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
    public void openHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivityForResult(intent, HISTORY_REQUEST_CODE);  // 使用forResult启动
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 确保检查正确的请求码和结果码
        if (requestCode == HISTORY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("url")) {
                String url = data.getStringExtra("url");
                if (url != null && !url.isEmpty()) {
                    webView.loadUrl(url);  // 加载选中的URL
                }
            }
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
