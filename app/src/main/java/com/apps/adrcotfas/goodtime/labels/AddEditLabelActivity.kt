/*
 * Copyright 2016-2021 Adrian Cotfas
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

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.bl.CurrentSessionManager
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.databinding.ActivityAddEditLabelsBinding
import com.apps.adrcotfas.goodtime.labels.AddEditLabelsAdapter.OnEditLabelListener
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.SimpleItemTouchHelperCallback
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.statistics.Utils.getInvalidLabelWithRandomColor
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.takisoft.colorpicker.ColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddEditLabelActivity : AppCompatActivity(), OnEditLabelListener {

    private val viewModel: LabelsViewModel by viewModels()
    private lateinit var labels: MutableList<Label>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AddEditLabelsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var labelToAdd: Label
    private lateinit var emptyState: LinearLayout
    private lateinit var addLabelView: EditText
    private lateinit var imageRight: FrameLayout
    private lateinit var imageLeft: ImageView
    private lateinit var imageLeftContainer: FrameLayout

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var currentSessionManager: CurrentSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: ActivityAddEditLabelsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_edit_labels)
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = binding.labelList
        emptyState = binding.emptyState
        addLabelView = binding.addLabel.text
        imageRight = binding.addLabel.imageRightContainer
        imageLeft = binding.addLabel.imageLeft
        imageLeftContainer = binding.addLabel.imageLeftContainer
        val labelsLiveData = viewModel.allLabels
        labelsLiveData.observe(this) { labels: List<Label> ->
            this.labels = labels as MutableList<Label>
            adapter = AddEditLabelsAdapter(this, this.labels, this)
            recyclerView.adapter = adapter
            val lm = LinearLayoutManager(this)
            lm.reverseLayout = true
            lm.stackFromEnd = true
            recyclerView.layoutManager = lm
            recyclerView.itemAnimator = DefaultItemAnimator()
            labelsLiveData.removeObservers(this@AddEditLabelActivity)
            binding.progressBar.visibility = View.GONE
            updateRecyclerViewVisibility()
            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
        labelToAdd = getInvalidLabelWithRandomColor(this)
        imageRight.setOnClickListener {
            addLabel()
            updateRecyclerViewVisibility()
            ThemeHelper.clearFocusEditText(addLabelView, this)
        }
        imageLeftContainer.setOnClickListener {
            ThemeHelper.requestFocusEditText(
                addLabelView,
                this
            )
        }
        addLabelView.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                imageRight.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                imageLeft.setImageDrawable(resources.getDrawable(if (hasFocus) R.drawable.ic_palette else R.drawable.ic_add))
                imageLeftContainer.setOnClickListener(if (hasFocus) View.OnClickListener {
                    val p = ColorPickerDialog.Params.Builder(this@AddEditLabelActivity)
                        .setColors(ThemeHelper.getPalette(this))
                        .setSelectedColor(ThemeHelper.getColor(this, labelToAdd.colorId))
                        .build()
                    val dialog = ColorPickerDialog(
                        this@AddEditLabelActivity,
                        R.style.DialogTheme,
                        { c: Int ->
                            labelToAdd.colorId = ThemeHelper.getIndexOfColor(this, c)
                            imageLeft.setColorFilter(c)
                        },
                        p
                    )
                    dialog.setTitle(R.string.label_select_color)
                    dialog.show()
                } else View.OnClickListener {
                    ThemeHelper.requestFocusEditText(
                        addLabelView,
                        this
                    )
                })
                imageLeft.setColorFilter(
                    ThemeHelper.getColor(
                        this,
                        if (hasFocus) labelToAdd.colorId else ThemeHelper.COLOR_INDEX_UNLABELED
                    )
                )
                if (!hasFocus) {
                    labelToAdd = getInvalidLabelWithRandomColor(this)
                    addLabelView.setText("")
                }
            }
        addLabelView.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addLabel()
                updateRecyclerViewVisibility()
                ThemeHelper.clearFocusEditText(addLabelView, this)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onEditLabel(label: String, newLabel: String) {
        viewModel.editLabelName(label, newLabel)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (crtLabel.title == label) {
            preferenceHelper.currentSessionLabel = Label(newLabel, crtLabel.colorId)
        }
    }

    override fun onEditColor(label: String, newColor: Int) {
        viewModel.editLabelColor(label, newColor)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (crtLabel.title != "" && crtLabel.title == label) {
            preferenceHelper.currentSessionLabel = Label(label, newColor)
        }
    }

    override fun onToggleArchive(label: Label, adapterPosition: Int) {
        viewModel.toggleLabelArchive(label.title, label.archived)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (label.archived && crtLabel.title != "" && crtLabel.title == label.title) {
            currentSessionManager.currentSession.setLabel("")
            preferenceHelper.currentSessionLabel = Label(
                "", ThemeHelper.getColor(
                    this,
                    ThemeHelper.COLOR_INDEX_UNLABELED
                )
            )
        }
        if (label.archived && !preferenceHelper.archivedLabelHintWasShown) {
            showArchivedLabelHint()
        }
    }

    /**
     * When archiving a label for the first time, a snackbar will be shown to explain the action.
     */
    private fun showArchivedLabelHint() {
        val s = Snackbar.make(
            recyclerView,
            getString(R.string.tutorial_archive_label),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(android.R.string.ok)) { preferenceHelper.archivedLabelHintWasShown = true }
        s.behavior = object : BaseTransientBottomBar.Behavior() {
            override fun canSwipeDismissView(child: View): Boolean {
                return false
            }
        }
        s.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDeleteLabel(label: Label, position: Int) {
        labels.remove(label)
        adapter.notifyItemRemoved(position)
        viewModel.deleteLabel(label.title)

        // workaround for edge case: clipping animation of last cached entry
        if (adapter.itemCount == 0) {
            adapter.notifyDataSetChanged()
        }
        val crtSessionLabel = currentSessionManager.currentSession.label.value
        if (crtSessionLabel != null && crtSessionLabel == label.title) {
            currentSessionManager.currentSession.setLabel("")
        }

        // the label attached to the current session was deleted
        if (label.title == preferenceHelper.currentSessionLabel.title) {
            preferenceHelper.currentSessionLabel = Label(
                "", ThemeHelper.getColor(
                    this,
                    ThemeHelper.COLOR_INDEX_UNLABELED
                )
            )
        }
        updateRecyclerViewVisibility()
    }

    /**
     * Update the order of the labels inside the database based on the
     * rearrangement that was done inside the adapter.
     */
    override fun onLabelRearranged() {
        for (i in labels.indices) {
            viewModel.editLabelOrder(labels[i].title, i)
        }
    }

    private fun updateRecyclerViewVisibility() {
        if (adapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun addLabel() {
        labelToAdd =
            Label(addLabelView.text.toString().trim { it <= ' ' }, labelToAdd.colorId)
        if (labelIsGoodToAdd(this, labels, labelToAdd.title, "")) {
            labels.add(labelToAdd)
            adapter.notifyItemInserted(labels.size)
            recyclerView.scrollToPosition(labels.size - 1)
            viewModel.addLabel(labelToAdd)
            labelToAdd = Label(
                "", ThemeHelper.getColor(
                    this,
                    ThemeHelper.COLOR_INDEX_UNLABELED
                )
            )
            addLabelView.setText("")
        }
    }

    companion object {
        private val TAG = AddEditLabelActivity::class.java.simpleName

        /**
         * Used for checking label names before adding or renaming existing ones.
         * Checks for invalid strings like empty ones, spaces or duplicates
         * @param newLabel The desired name for the new label or renamed label
         * @return true if the label name is valid, false otherwise
         */
        @JvmStatic
        fun labelIsGoodToAdd(
            context: Context,
            labels: List<Label>,
            newLabel: String,
            beforeEdit: String
        ): Boolean {
            var result = true
            if (beforeEdit != "" && beforeEdit == newLabel) {
                result = false
            } else if (newLabel.isEmpty()) {
                result = false
            } else {
                var duplicateFound = false
                for (l in labels) {
                    if (newLabel == l.title) {
                        duplicateFound = true
                        break
                    }
                }
                if (duplicateFound) {
                    Toast.makeText(context, R.string.label_already_exists, Toast.LENGTH_SHORT)
                        .show()
                    result = false
                }
            }
            return result
        }
    }
}