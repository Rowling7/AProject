package com.example.simplebrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ScriptEditorActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText codeEditText;
    private EditText matchEditText;
    private String originalScriptName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_editor);

        nameEditText = findViewById(R.id.nameEditText);
        codeEditText = findViewById(R.id.codeEditText);
        matchEditText = findViewById(R.id.matchEditText);

        originalScriptName = getIntent().getStringExtra("script_name");
        if (originalScriptName != null) {
            loadScriptForEditing(originalScriptName);
        } else {
            codeEditText.setText("// 在这里编写你的脚本\n// 例如：\ndocument.querySelectorAll('.ad').forEach(el => el.remove());");
            matchEditText.setText("*");
        }

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this::saveScript);
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
