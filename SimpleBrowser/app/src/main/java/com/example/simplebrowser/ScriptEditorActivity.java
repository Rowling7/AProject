package com.example.simplebrowser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScriptEditorActivity extends AppCompatActivity {
    private static final int PICK_TXT_FILE = 1001;
    private EditText nameEditText;
    private EditText codeEditText;
    private EditText matchEditText;
    private String originalScriptName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_editor);

        // 初始化视图
        nameEditText = findViewById(R.id.nameEditText);
        codeEditText = findViewById(R.id.codeEditText);
        matchEditText = findViewById(R.id.matchEditText);
        Button saveButton = findViewById(R.id.saveButton);
        Button importButton = findViewById(R.id.importButton);

        // 加载现有脚本或设置默认值
        originalScriptName = getIntent().getStringExtra("script_name");
        if (originalScriptName != null) {
            loadScriptForEditing(originalScriptName);
        } else {
            setDefaultScriptContent();
        }

        // 设置按钮点击事件
        saveButton.setOnClickListener(this::saveScript);
        importButton.setOnClickListener(v -> openFilePicker());
    }

    private void setDefaultScriptContent() {
        codeEditText.setText("// 在这里编写你的脚本\n// 例如：\ndocument.querySelectorAll('.ad').forEach(el => el.remove());");
        matchEditText.setText("*");
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_TXT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_TXT_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                loadTxtFile(data.getData());
            }
        }
    }

    private void loadTxtFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            // 从文件名自动设置脚本名称
            String fileName = uri.getLastPathSegment();
            if (fileName != null && fileName.endsWith(".txt")) {
                fileName = fileName.substring(0, fileName.length() - 4);
                nameEditText.setText(fileName);
            }

            codeEditText.setText(stringBuilder.toString());
            Toast.makeText(this, "脚本导入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "脚本导入失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadScriptForEditing(String scriptName) {
        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        String scriptsJson = prefs.getString("scripts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(scriptsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.getString("name").equals(scriptName)) {
                    nameEditText.setText(json.getString("name"));
                    codeEditText.setText(json.getString("code"));
                    matchEditText.setText(json.getString("matchPattern"));
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "加载脚本失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveScript(View view) {
        String name = nameEditText.getText().toString().trim();
        String code = codeEditText.getText().toString().trim();
        String match = matchEditText.getText().toString().trim();

        if (name.isEmpty() || code.isEmpty() || match.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        String scriptsJson = prefs.getString("scripts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(scriptsJson);

            // 如果是编辑现有脚本，先移除旧版本
            if (originalScriptName != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (jsonArray.getJSONObject(i).getString("name").equals(originalScriptName)) {
                        jsonArray.remove(i);
                        break;
                    }
                }
            }

            // 添加新脚本
            JSONObject newScript = new JSONObject();
            newScript.put("name", name);
            newScript.put("code", code);
            newScript.put("matchPattern", match);
            newScript.put("enabled", true); // 新脚本默认启用
            jsonArray.put(newScript);

            prefs.edit().putString("scripts", jsonArray.toString()).apply();
            Toast.makeText(this, "脚本已保存", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
