package com.apps.adrcotfas.goodtime.Statistics.AllSessions;

import android.view.View;

import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllEntriesRowBinding;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AllSessionsViewHolder extends RecyclerView.ViewHolder {
    private StatisticsAllEntriesRowBinding binding;
    public View rowOverlay;

    public AllSessionsViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
        rowOverlay = binding.overlay;
    }

    public void bind(Session item, int color) {
        binding.setItem(item);
        binding.setColor(color);
    }
}
