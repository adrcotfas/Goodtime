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
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.labels.AddEditLabelsAdapter.OnEditLabelListener
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.bl.CurrentSessionManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.main.SimpleItemTouchHelperCallback
import android.view.View.OnFocusChangeListener
import com.takisoft.colorpicker.ColorPickerDialog
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.BaseTransientBottomBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.databinding.ActivityAddEditLabelsBinding
import com.apps.adrcotfas.goodtime.statistics.Utils.getInvalidLabel

@AndroidEntryPoint
class AddEditLabelActivity : AppCompatActivity(), OnEditLabelListener {

    private lateinit var mLabelsViewModel: LabelsViewModel
    private lateinit var mLabels: MutableList<Label>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mCustomAdapter: AddEditLabelsAdapter
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private lateinit var mLabelToAdd: Label
    private lateinit var mEmptyState: LinearLayout
    private lateinit var mAddLabelView: EditText
    private lateinit var mImageRightContainer: FrameLayout
    private lateinit var mImageLeft: ImageView
    private lateinit var mImageLeftContainer: FrameLayout

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var currentSessionManager: CurrentSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: ActivityAddEditLabelsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_edit_labels)
        mLabelsViewModel = ViewModelProvider(this).get(LabelsViewModel::class.java)
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        mRecyclerView = binding.labelList
        mEmptyState = binding.emptyState
        mAddLabelView = binding.addLabel.text
        mImageRightContainer = binding.addLabel.imageRightContainer
        mImageLeft = binding.addLabel.imageLeft
        mImageLeftContainer = binding.addLabel.imageLeftContainer
        val labelsLiveData = mLabelsViewModel.allLabels
        labelsLiveData.observe(this, { labels: List<Label> ->
            mLabels = labels as MutableList<Label>
            mCustomAdapter = AddEditLabelsAdapter(this, mLabels, this)
            mRecyclerView.adapter = mCustomAdapter
            val lm = LinearLayoutManager(this)
            lm.reverseLayout = true
            lm.stackFromEnd = true
            mRecyclerView.layoutManager = lm
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            labelsLiveData.removeObservers(this@AddEditLabelActivity)
            binding.progressBar.visibility = View.GONE
            updateRecyclerViewVisibility()
            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(mCustomAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper.attachToRecyclerView(mRecyclerView)
        })
        mLabelToAdd = getInvalidLabel(this)
        mImageRightContainer.setOnClickListener {
            addLabel()
            updateRecyclerViewVisibility()
            ThemeHelper.clearFocusEditText(mAddLabelView, this)
        }
        mImageLeftContainer.setOnClickListener {
            ThemeHelper.requestFocusEditText(
                mAddLabelView,
                this
            )
        }
        mAddLabelView.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                mImageRightContainer.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                mImageLeft.setImageDrawable(resources.getDrawable(if (hasFocus) R.drawable.ic_palette else R.drawable.ic_add))
                mImageLeftContainer.setOnClickListener(if (hasFocus) View.OnClickListener {
                    val p = ColorPickerDialog.Params.Builder(this@AddEditLabelActivity)
                        .setColors(ThemeHelper.getPalette(this))
                        .setSelectedColor(ThemeHelper.getColor(this, mLabelToAdd.colorId))
                        .build()
                    val dialog = ColorPickerDialog(
                        this@AddEditLabelActivity,
                        R.style.DialogTheme,
                        { c: Int ->
                            mLabelToAdd.colorId = ThemeHelper.getIndexOfColor(this, c)
                            mImageLeft.setColorFilter(c)
                        },
                        p
                    )
                    dialog.setTitle(R.string.label_select_color)
                    dialog.show()
                } else View.OnClickListener {
                    ThemeHelper.requestFocusEditText(
                        mAddLabelView,
                        this
                    )
                })
                mImageLeft.setColorFilter(
                    ThemeHelper.getColor(
                        this,
                        if (hasFocus) mLabelToAdd.colorId else ThemeHelper.COLOR_INDEX_UNLABELED
                    )
                )
                if (!hasFocus) {
                    mLabelToAdd = getInvalidLabel(this)
                    mAddLabelView.setText("")
                }
            }
        mAddLabelView.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addLabel()
                updateRecyclerViewVisibility()
                ThemeHelper.clearFocusEditText(mAddLabelView, this)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper.startDrag(viewHolder)
    }

    override fun onEditLabel(label: String, newLabel: String) {
        mLabelsViewModel.editLabelName(label, newLabel)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (crtLabel.title == label) {
            preferenceHelper.currentSessionLabel = Label(newLabel, crtLabel.colorId)
        }
    }

    override fun onEditColor(label: String, newColor: Int) {
        mLabelsViewModel.editLabelColor(label, newColor)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (crtLabel.title != "" && crtLabel.title == label) {
            preferenceHelper.currentSessionLabel = Label(label, newColor)
        }
    }

    override fun onToggleArchive(label: Label, adapterPosition: Int) {
        mLabelsViewModel.toggleLabelArchive(label.title, label.archived)
        val crtLabel = preferenceHelper.currentSessionLabel
        if (label.archived && crtLabel.title != "" && crtLabel.title == label.title) {
            currentSessionManager.currentSession.setLabel("")
            preferenceHelper.currentSessionLabel = Label("", ThemeHelper.getColor(
                this,
                ThemeHelper.COLOR_INDEX_UNLABELED
            ))
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
            mRecyclerView,
            getString(R.string.tutorial_archive_label),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") { preferenceHelper.archivedLabelHintWasShown = true }
            .setActionTextColor(resources.getColor(R.color.teal200))
        s.behavior = object : BaseTransientBottomBar.Behavior() {
            override fun canSwipeDismissView(child: View): Boolean {
                return false
            }
        }
        val tv = s.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        tv?.setTextColor(ContextCompat.getColor(this, R.color.white))
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
        mLabels.remove(label)
        mCustomAdapter.notifyItemRemoved(position)
        mLabelsViewModel.deleteLabel(label.title)

        // workaround for edge case: clipping animation of last cached entry
        if (mCustomAdapter.itemCount == 0) {
            mCustomAdapter.notifyDataSetChanged()
        }
        val crtSessionLabel = currentSessionManager.currentSession.label.value
        if (crtSessionLabel != null && crtSessionLabel == label.title) {
            currentSessionManager.currentSession.setLabel("")
        }

        // the label attached to the current session was deleted
        if (label.title == preferenceHelper.currentSessionLabel.title) {
            preferenceHelper.currentSessionLabel = Label("", ThemeHelper.getColor(
                this,
                ThemeHelper.COLOR_INDEX_UNLABELED
            ))
        }
        updateRecyclerViewVisibility()
    }

    /**
     * Update the order of the labels inside the database based on the
     * rearrangement that was done inside the adapter.
     */
    override fun onLabelRearranged() {
        for (i in mLabels.indices) {
            mLabelsViewModel.editLabelOrder(mLabels[i].title, i)
        }
    }

    private fun updateRecyclerViewVisibility() {
        if (mCustomAdapter.itemCount == 0) {
            mRecyclerView.visibility = View.GONE
            mEmptyState.visibility = View.VISIBLE
        } else {
            mRecyclerView.visibility = View.VISIBLE
            mEmptyState.visibility = View.GONE
        }
    }

    private fun addLabel() {
        mLabelToAdd =
            Label(mAddLabelView.text.toString().trim { it <= ' ' }, mLabelToAdd.colorId)
        if (labelIsGoodToAdd(this, mLabels, mLabelToAdd.title, "")) {
            mLabels.add(mLabelToAdd)
            mCustomAdapter.notifyItemInserted(mLabels.size)
            mRecyclerView.scrollToPosition(mLabels.size - 1)
            mLabelsViewModel.addLabel(mLabelToAdd)
            mLabelToAdd = Label("", ThemeHelper.getColor(
                this,
                ThemeHelper.COLOR_INDEX_UNLABELED
            ))
            mAddLabelView.setText("")
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