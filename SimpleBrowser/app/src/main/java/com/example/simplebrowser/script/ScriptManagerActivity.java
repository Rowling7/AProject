package com.example.simplebrowser.scrpit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.simplebrowser.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ScriptManagerActivity extends AppCompatActivity {
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
