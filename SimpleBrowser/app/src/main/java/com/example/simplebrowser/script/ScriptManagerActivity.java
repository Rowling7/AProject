package com.example.simplebrowser.script;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.simplebrowser.R;
import com.example.simplebrowser.script.UserScript;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScriptManagerActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CREATE_DOCUMENT = 1; // 新增常量
    private ListView scriptsListView;
    private List<UserScript> userScripts = new ArrayList<>();
    private ScriptAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_manager);

        scriptsListView = findViewById(R.id.scriptsListView);
        adapter = new ScriptAdapter(this, userScripts);
        scriptsListView.setAdapter(adapter);

        loadScripts();

        Button addScriptButton = findViewById(R.id.addScriptButton);
        addScriptButton.setOnClickListener(v -> {
            startActivity(new Intent(ScriptManagerActivity.this, ScriptEditorActivity.class));
        });

        // 导出按钮点击事件
        Button exportScriptButton = findViewById(R.id.exportScriptButton);
        exportScriptButton.setOnClickListener(v -> {
            // 启动文件创建意图让用户选择保存位置
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "scripts.txt");
            startActivityForResult(intent, REQUEST_CREATE_DOCUMENT);
        });
    }

    // 使用 onActivityResult 处理用户选择的文件 URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_DOCUMENT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            exportScriptsToFile(uri);
        }
    }

    // 导出脚本到用户选择的文件
    private void exportScriptsToFile(Uri uri) {
        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        String scriptsJson = prefs.getString("scripts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(scriptsJson);
            StringBuilder content = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                content.append("Name: ").append(json.getString("name")).append("\n");
                content.append("Code: ").append(json.getString("code")).append("\n");
                content.append("Match Pattern: ").append(json.getString("matchPattern")).append("\n");
                content.append("Enabled: ").append(json.getBoolean("enabled") ? "Yes" : "No").append("\n\n");
            }

            // 写入文件
            try (FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                if (fos != null) {
                    fos.write(content.toString().getBytes());
                    Toast.makeText(this, "脚本已导出至 " + uri.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "解析脚本失败", Toast.LENGTH_SHORT).show();
        }
    }

    private class ScriptAdapter extends ArrayAdapter<UserScript> {
        public ScriptAdapter(Context context, List<UserScript> scripts) {
            super(context, R.layout.item_script, scripts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_script, parent, false);
            }

            UserScript script = getItem(position);
            TextView nameView = convertView.findViewById(R.id.scriptNameTextView);
            Button toggleBtn = convertView.findViewById(R.id.toggleButton);
            Button editBtn = convertView.findViewById(R.id.editButton);
            Button deleteBtn = convertView.findViewById(R.id.deleteButton);

            nameView.setText(script.getName() + (script.isEnabled() ? " (启用)" : " (禁用)"));
            toggleBtn.setText(script.isEnabled() ? "停用" : "启用");

            toggleBtn.setOnClickListener(v -> {
                script.setEnabled(!script.isEnabled());
                saveScripts();
                notifyDataSetChanged();
                Toast.makeText(ScriptManagerActivity.this,
                        "脚本已" + (script.isEnabled() ? "启用" : "停用"), Toast.LENGTH_SHORT).show();
            });

            editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ScriptManagerActivity.this, ScriptEditorActivity.class);
                intent.putExtra("script_name", script.getName());
                startActivity(intent);
            });

            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(ScriptManagerActivity.this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除脚本 " + script.getName() + " 吗?")
                        .setPositiveButton("删除", (dialog, which) -> {
                            userScripts.remove(position);
                            saveScripts();
                            notifyDataSetChanged();
                            Toast.makeText(ScriptManagerActivity.this,
                                    "脚本已删除", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

            return convertView;
        }
    }

    private void loadScripts() {
        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        String scriptsJson = prefs.getString("scripts", "[]");

        try {
            JSONArray jsonArray = new JSONArray(scriptsJson);
            userScripts.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                userScripts.add(new UserScript(
                        json.getString("name"),
                        json.getString("code"),
                        json.getString("matchPattern"),
                        json.getBoolean("enabled")
                ));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveScripts() {
        SharedPreferences prefs = getSharedPreferences("user_scripts", MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for (UserScript script : userScripts) {
            try {
                JSONObject json = new JSONObject();
                json.put("name", script.getName());
                json.put("code", script.getCode());
                json.put("matchPattern", script.getMatchPattern());
                json.put("enabled", script.isEnabled());
                jsonArray.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        prefs.edit().putString("scripts", jsonArray.toString()).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScripts();
    }
}