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
package com.apps.adrcotfas.goodtime.statistics.all_sessions

import android.content.res.ColorStateList
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllSessionsRowBinding

class AllSessionsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(
    itemView
) {
    private var binding: StatisticsAllSessionsRowBinding = DataBindingUtil.bind(itemView)!!

    val rowOverlay: View = binding.overlay
    fun bind(item: Session, color: ColorStateList) {
        binding.item = item
        binding.status.chipBackgroundColor = color
    }

}