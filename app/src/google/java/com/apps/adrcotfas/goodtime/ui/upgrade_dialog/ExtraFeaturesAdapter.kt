/*
 * Copyright (C) 2020-2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apps.adrcotfas.goodtime.ui.upgrade_dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.util.ThemeHelper

class ExtraFeaturesAdapter(
        private val context: Context,
        private val data : List<Pair<String, Int>>)
    : RecyclerView.Adapter<ExtraFeaturesAdapter.ViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.text)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        fun bind(context : Context, item: Pair<String, Int>) {
            text.text = item.first
            icon.setImageDrawable(context.resources.getDrawable(item.second))
            icon.setColorFilter(ThemeHelper.getColor(context, ThemeHelper.COLOR_INDEX_UNLABELED))
        }

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.dialog_upgrade_row, parent, false)
                return ViewHolder(view)
            }
        }
    }
}