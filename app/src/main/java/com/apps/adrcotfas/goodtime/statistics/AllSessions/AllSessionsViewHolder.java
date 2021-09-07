/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.statistics.AllSessions;

import android.content.res.ColorStateList;
import android.view.View;

import com.apps.adrcotfas.goodtime.database.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllSessionsRowBinding;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AllSessionsViewHolder extends RecyclerView.ViewHolder {
    private final StatisticsAllSessionsRowBinding binding;
    final View rowOverlay;

    AllSessionsViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
        rowOverlay = binding.overlay;
    }

    public void bind(Session item, ColorStateList color) {
        binding.setItem(item);
        binding.status.setChipBackgroundColor(color);
    }
}
