package com.example.simplebrowser.history;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplebrowser.MainActivity;
import com.example.simplebrowser.browser.BrowserActivity;
import com.example.simplebrowser.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ListView historyListView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems = new ArrayList<>();
    private Button btnDeleteSelected, btnDeleteAll, btnCancelSelection;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnCancelSelection = findViewById(R.id.btnCancelSelection);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        overridePendingTransition(0, 0);  // 添加这行
        loadHistory();
        setupListView();
        setupButtons();
        setupBottomNavigation();

    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("browser_history", MODE_PRIVATE);
        String historyJson = prefs.getString("history", "[]");

        try {
            JSONArray jsonArray = new JSONArray(historyJson);
            historyItems.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                historyItems.add(new HistoryItem(
                        json.getString("title"),
                        json.getString("url"),
                        json.getLong("timestamp")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("browser_history", MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for (HistoryItem item : historyItems) {
            try {
                JSONObject json = new JSONObject();
                json.put("title", item.getTitle());
                json.put("url", item.getUrl());
                json.put("timestamp", item.getTimestamp());
                jsonArray.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        prefs.edit().putString("history", jsonArray.toString()).apply();
    }

    private void setupListView() {
        adapter = new HistoryAdapter(this, historyItems);
        historyListView.setAdapter(adapter);

        // 长按删除单项
        historyListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });

        // 点击打开URL
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            if (!adapter.isSelectionMode()) {
                String url = historyItems.get(position).getUrl();
                Intent browserIntent = new Intent(HistoryActivity.this, BrowserActivity.class);
                browserIntent.putExtra("url", url);
                overridePendingTransition(0, 0);//取消动画
                startActivity(browserIntent);
            }
        });

        // 多选模式
        historyListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        historyListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                adapter.toggleSelection(position);
                updateSelectionCount(mode);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.history_context_menu, menu);
                btnDeleteSelected.setVisibility(View.VISIBLE);
                btnCancelSelection.setVisibility(View.VISIBLE);
                btnDeleteAll.setVisibility(View.GONE);
                adapter.setSelectionMode(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.menu_delete) {
                    deleteSelectedItems();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelections();
                adapter.setSelectionMode(false);
                btnDeleteSelected.setVisibility(View.GONE);
                btnCancelSelection.setVisibility(View.GONE);
                btnDeleteAll.setVisibility(View.VISIBLE);
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
            Intent homeIntent = new Intent(this, MainActivity.class);
            overridePendingTransition(0, 0);//取消动画
            startActivity(homeIntent);
            return true;
        } else if (id == R.id.nav_history) {
            // 已经在历史页面，无需处理
            return true;
        } else if (id == R.id.nav_settings) {
            // 启动设置页面（假设有SettingsActivity）
            Intent settingsIntent = new Intent(this, MainActivity.class);
            overridePendingTransition(0, 0);//取消动画
            startActivity(settingsIntent);
            return true;
        }
        return false;
    });
    bottomNavigationView.setSelectedItemId(R.id.nav_history);
}


    private void setupButtons() {
        btnDeleteAll.setOnClickListener(v -> showDeleteAllDialog());

        btnDeleteSelected.setOnClickListener(v -> {
            if (adapter.getSelectedCount() > 0) {
                deleteSelectedItems();
            } else {
                Toast.makeText(this, "请先选择项目", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelSelection.setOnClickListener(v -> {
            historyListView.clearChoices();
            adapter.clearSelections();
            adapter.notifyDataSetChanged();
            btnDeleteSelected.setVisibility(View.GONE);
            btnCancelSelection.setVisibility(View.GONE);
            btnDeleteAll.setVisibility(View.VISIBLE);
        });
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除这条记录吗?")
                .setPositiveButton("删除", (dialog, which) -> {
                    historyItems.remove(position);
                    saveHistory();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除全部")
                .setMessage("确定要删除所有历史记录吗?")
                .setPositiveButton("删除", (dialog, which) -> {
                    historyItems.clear();
                    saveHistory();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteSelectedItems() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        for (int i = selectedPositions.size() - 1; i >= 0; i--) {
            historyItems.remove(selectedPositions.get(i).intValue());
        }
        saveHistory();
        adapter.notifyDataSetChanged();
    }

    private void updateSelectionCount(ActionMode mode) {
        int count = adapter.getSelectedCount();
        mode.setTitle(count + " 已选择");
    }
}
