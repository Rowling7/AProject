package com.example.simplebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplebrowser.browser.BrowserActivity;
import com.example.simplebrowser.history.HistoryActivity;
import com.example.simplebrowser.script.ScriptManagerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private RadioGroup engineRadioGroup;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        engineRadioGroup = findViewById(R.id.engineRadioGroup);
        Button searchButton = findViewById(R.id.searchButton);
        Button historyButton = findViewById(R.id.historyButton);
        Button scriptManagerButton = findViewById(R.id.scriptManagerButton);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        overridePendingTransition(0, 0);  // 添加这行
        // 设置底部导航栏
        setupBottomNavigation();

        // 设置历史按钮点击事件
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // 设置脚本管理按钮点击事件
        scriptManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScriptManagerActivity.class);
            overridePendingTransition(0, 0);//取消动画
            startActivity(intent);
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_back) {
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                // 已经在主页，无需处理
                return true;
            } else if (id == R.id.nav_history) {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);//取消动画
                return true;
            } else if (id == R.id.nav_settings) {
                // 这里可以添加设置页面的跳转逻辑
                // 暂时返回到主页
                return true;
            }
            return false;
        });

        // 高亮显示当前选中的主页菜单项
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        int selectedId = engineRadioGroup.getCheckedRadioButtonId();
        String engine = ((RadioButton) findViewById(selectedId)).getText().toString();

        String url;
        if (isUrl(query)) {
            if (query.startsWith("http://") || query.startsWith("https://")) {
                url = query;
            } else {
                url = "https://" + query;
            }
        } else {
            if (engine.equals(getString(R.string.baidu))) {
                url = "https://www.baidu.com/s?wd=" + query;
            } else {
                url = "https://www.bing.com/search?q=" + query;
            }
        }

        Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private boolean isUrl(String input) {
        final Pattern URL_PATTERN = Pattern.compile(
                "^(https?:\\/\\/)?" + // 协议
                        "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" + // 域名
                        "((\\d{1,3}\\.){3}\\d{1,3}))" + // 或 IP 地址
                        "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" + // 端口和路径
                        "(\\?[;&a-z\\d%_.~+=-]*)?" + // 查询字符串
                        "(\\#[-a-z\\d_]*)?$", Pattern.CASE_INSENSITIVE);
        return URL_PATTERN.matcher(input).matches();
    }
}
