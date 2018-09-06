package com.apps.adrcotfas.goodtime.Statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesAdapter extends RecyclerView.Adapter<AllEntriesViewHolder> {
    private List<Session> items;

    public AllEntriesAdapter(List<Session> items) {
        this.items = items;
    }

    @Override
    public AllEntriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View statusContainer = inflater.inflate(R.layout.statistics_all_entries_row, parent, false);
        return new AllEntriesViewHolder(statusContainer);
    }

    @Override
    public void onBindViewHolder(AllEntriesViewHolder holder, int position) {
        Session status = items.get(position);
        holder.bind(status);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}