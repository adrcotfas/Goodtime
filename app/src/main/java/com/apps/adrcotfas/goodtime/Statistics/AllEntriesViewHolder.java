package com.apps.adrcotfas.goodtime.Statistics;

import android.view.View;

import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllEntriesRowBinding;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesViewHolder extends RecyclerView.ViewHolder {
    private StatisticsAllEntriesRowBinding binding;
    public View rowOverlay;

    public AllEntriesViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
        rowOverlay = binding.rowOverlay;
    }

    public void bind(Session item) {
        binding.setItem(item);
    }
}
