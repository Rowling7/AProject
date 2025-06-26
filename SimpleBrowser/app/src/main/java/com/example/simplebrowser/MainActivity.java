package com.example.simplebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RadioGroup engineRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        engineRadioGroup = findViewById(R.id.engineRadioGroup);
        Button searchButton = findViewById(R.id.searchButton);
        Button scriptManagerButton = findViewById(R.id.scriptManagerButton);
        scriptManagerButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScriptManagerActivity.class));
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

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        int selectedId = engineRadioGroup.getCheckedRadioButtonId();
        String engine = ((RadioButton) findViewById(selectedId)).getText().toString();

        String url;
        if (isUrl(query)) {
            // 修改这里：默认使用 https:// 而不是 http://
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
