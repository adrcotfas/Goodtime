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
package com.apps.adrcotfas.goodtime.labels

import android.annotation.SuppressLint
import android.content.Context
import com.apps.adrcotfas.goodtime.labels.AddEditLabelActivity.Companion.labelIsGoodToAdd
import com.apps.adrcotfas.goodtime.main.ItemTouchHelperAdapter
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import androidx.core.content.ContextCompat
import com.apps.adrcotfas.goodtime.main.ItemTouchHelperViewHolder
import android.view.View.OnFocusChangeListener
import android.content.DialogInterface
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.database.Label
import com.takisoft.colorpicker.ColorPickerDialog
import java.lang.ref.WeakReference
import java.util.*

class AddEditLabelsAdapter(
    context: Context,
    private val labels: List<Label>,
    private val callback: OnEditLabelListener
) : RecyclerView.Adapter<AddEditLabelsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    interface OnEditLabelListener {
        fun onEditColor(label: String, newColor: Int)
        fun onEditLabel(label: String, newLabel: String)
        fun onDeleteLabel(label: Label, position: Int)
        fun onLabelRearranged()
        fun onToggleArchive(label: Label, adapterPosition: Int)
        fun onDragStarted(viewHolder: RecyclerView.ViewHolder)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val context: WeakReference<Context> = WeakReference(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.activity_add_edit_labels_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crtLabel = labels[position]
        holder.text.setText(crtLabel.title)
        holder.imageLeft.setColorFilter(ThemeHelper.getColor(context.get()!!, crtLabel.colorId))
        holder.labelIcon.setImageDrawable(
            ContextCompat.getDrawable(
                context.get()!!,
                if (crtLabel.archived) R.drawable.ic_label_off else R.drawable.ic_label
            )
        )
        holder.labelIcon.setColorFilter(ThemeHelper.getColor(context.get()!!, crtLabel.colorId))
        holder.scrollIconContainer.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                callback.onDragStarted(holder)
            }
            false
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(labels, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(labels, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onClearView() {
        callback.onLabelRearranged()
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemTouchHelperViewHolder {
        val text: EditText = itemView.findViewById(R.id.text)
        val labelIcon: ImageView = itemView.findViewById(R.id.label_icon)
        val imageLeft: ImageView = itemView.findViewById(R.id.image_left)
        private val imageRight: ImageView = itemView.findViewById(R.id.image_right)
        private val row: RelativeLayout = itemView.findViewById(R.id.dialog_edit_label_row)
        val scrollIconContainer: FrameLayout = itemView.findViewById(R.id.scroll_icon_container)
        private val imageLeftContainer: FrameLayout = itemView.findViewById(R.id.image_left_container)
        private val labelIconContainer: FrameLayout = itemView.findViewById(R.id.label_icon_container)
        private val imageRightContainer: FrameLayout = itemView.findViewById(R.id.image_right_container)
        private val imageDeleteContainer: FrameLayout = itemView.findViewById(R.id.image_delete_container)

        override fun onItemSelected() {
            row.elevation = 4f
        }

        override fun onItemClear() {
            row.elevation = 0f
        }

        init {
            // the palette icon
            // can have the edit or the done icon

            // switch the focus to a different row
            text.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->

                // shrink the textView when we're in edit mode and the delete button appears
                val params = text.layoutParams as RelativeLayout.LayoutParams
                params.addRule(
                    RelativeLayout.START_OF,
                    if (hasFocus) R.id.image_delete_container else R.id.image_right_container
                )
                text.layoutParams = params
                val position = bindingAdapterPosition
                val crtLabel = labels[position]
                labelIcon.setColorFilter(ThemeHelper.getColor(context.get()!!, crtLabel.colorId))
                imageLeftContainer.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                labelIconContainer.visibility = if (hasFocus) View.INVISIBLE else View.VISIBLE
                imageDeleteContainer.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                imageRight.setImageDrawable(
                    ContextCompat.getDrawable(
                        context.get()!!, if (hasFocus) R.drawable.ic_done else R.drawable.ic_edit
                    )
                )
                // the done button or the edit button (depending on focus)
                imageRightContainer.setOnClickListener(if (hasFocus) View.OnClickListener {
                    ThemeHelper.clearFocusEditText(
                        text,
                        context.get()!!
                    )
                } else View.OnClickListener {
                    ThemeHelper.requestFocusEditText(
                        text,
                        context.get()!!
                    )
                })
                if (!hasFocus) {
                    val newLabelName = text.text.toString().trim { it <= ' ' }
                    // save a title when losing focus if any changes were made
                    if (labelIsGoodToAdd(context.get()!!, labels, newLabelName, crtLabel.title)) {
                        callback.onEditLabel(crtLabel.title, newLabelName)
                        crtLabel.title = newLabelName
                        notifyItemChanged(position)
                    } else {
                        text.setText(crtLabel.title)
                    }
                }
            }

            // delete a label
            imageDeleteContainer.setOnClickListener {
                val position = bindingAdapterPosition
                ThemeHelper.clearFocusEditText(text, context.get()!!)
                AlertDialog.Builder(context.get()!!)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.dialog_delete) { _: DialogInterface?, _: Int ->
                        callback.onDeleteLabel(
                            labels[position], position
                        )
                    }
                    .setTitle(R.string.label_delete_title)
                    .setMessage(R.string.label_delete_message)
                    .create().show()
            }

            // save the changes by clearing the focus
            text.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ThemeHelper.clearFocusEditText(text, context.get()!!)
                    return@setOnEditorActionListener true
                }
                false
            }

            // archive and unarchive a label
            labelIconContainer.setOnClickListener {
                val crtLabel = labels[bindingAdapterPosition]
                crtLabel.archived = !crtLabel.archived
                callback.onToggleArchive(crtLabel, bindingAdapterPosition)
                labelIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context.get()!!,
                        if (crtLabel.archived) R.drawable.ic_label_off else R.drawable.ic_label
                    )
                )
            }

            // changing the colorId of a label
            imageLeftContainer.setOnClickListener {
                val crtLabel = labels[bindingAdapterPosition]
                val p = ColorPickerDialog.Params.Builder(context.get())
                    .setColors(ThemeHelper.getPalette(context.get()!!))
                    .setSelectedColor(ThemeHelper.getColor(context.get()!!, crtLabel.colorId))
                    .build()
                val dialog = ColorPickerDialog(context.get()!!, R.style.DialogTheme, { c: Int ->
                    callback.onEditColor(
                        crtLabel.title,
                        ThemeHelper.getIndexOfColor(context.get()!!, c)
                    )
                    imageLeft.setColorFilter(c)
                    crtLabel.colorId = ThemeHelper.getIndexOfColor(context.get()!!, c)
                }, p)
                dialog.setTitle(R.string.label_select_color)
                dialog.show()
            }

            // the edit button
            imageRightContainer.setOnClickListener {
                ThemeHelper.requestFocusEditText(
                    text,
                    context.get()!!
                )
            }
        }
    }

    init {
        // update the order inside
        for (i in this.labels.indices) {
            this.labels[i].order = i
        }
    }
}