package com.example.simplebrowser.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.simplebrowser.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private List<HistoryItem> items;
    private Set<Integer> selectedPositions = new HashSet<>();
    private boolean isSelectionMode = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HistoryAdapter(Context context, List<HistoryItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public HistoryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvUrl = convertView.findViewById(R.id.tvUrl);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HistoryItem item = getItem(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvUrl.setText(item.getUrl());
        holder.tvTime.setText(dateFormat.format(new Date(item.getTimestamp())));

        if (isSelectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedPositions.contains(position));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        if (!selectionMode) {
            clearSelections();
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvUrl;
        TextView tvTime;
        CheckBox checkBox;
    }
}
