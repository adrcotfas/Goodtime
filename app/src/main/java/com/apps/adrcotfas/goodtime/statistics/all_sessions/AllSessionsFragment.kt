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
package com.apps.adrcotfas.goodtime.statistics.all_sessions

import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import androidx.lifecycle.LiveData
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.statistics.main.RecyclerItemClickListener
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog
import com.apps.adrcotfas.goodtime.statistics.main.StatisticsActivity
import android.content.DialogInterface
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentAllSessionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class AllSessionsFragment : Fragment(), OnLabelSelectedListener {
    private var mAdapter: AllSessionsAdapter? = null
    private var mActionMode: ActionMode? = null
    private var mSelectedEntries: MutableList<Long> = ArrayList()
    private var mIsMultiSelect = false
    private var mMenu: Menu? = null

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val labelsViewModel: LabelsViewModel by activityViewModels()

    private var mSessionToEdit: Session? = null
    private var mSessions: List<Session> = ArrayList()
    private lateinit var sessionsLiveDataAll: LiveData<List<Session>>
    private lateinit var sessionsLiveDataUnlabeled: LiveData<List<Session>>
    private lateinit var sessionsLiveDataCrtLabel: LiveData<List<Session>>
    private var mEmptyState: LinearLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mProgressBar: ProgressBar? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: StatisticsFragmentAllSessionsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.statistics_fragment_all_sessions,
            container,
            false
        )
        sessionsLiveDataAll = sessionViewModel.allSessions
        sessionsLiveDataUnlabeled = sessionViewModel.allSessionsUnlabeled
        if (labelsViewModel.crtExtendedLabel.value != null) {
            sessionsLiveDataCrtLabel =
                sessionViewModel.getSessions(labelsViewModel.crtExtendedLabel.value!!.title)
        }
        mEmptyState = binding.emptyState
        mProgressBar = binding.progressBar
        val view = binding.root
        mRecyclerView = binding.mainRecylcerView
        mRecyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        labelsViewModel.labels.observe(viewLifecycleOwner) { labels: List<Label> ->
            mAdapter = AllSessionsAdapter(labels)
            mRecyclerView!!.adapter = mAdapter
            labelsViewModel.crtExtendedLabel.observe(
                viewLifecycleOwner
            ) { refreshCurrentLabel() }
            mRecyclerView!!.addItemDecoration(
                DividerItemDecoration(
                    activity, LinearLayoutManager.VERTICAL
                )
            )
            mRecyclerView!!.addOnItemTouchListener(
                RecyclerItemClickListener(
                    activity,
                    mRecyclerView!!,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            if (mIsMultiSelect) {
                                multiSelect(position)
                            }
                        }

                        override fun onItemLongClick(view: View, position: Int) {
                            if (!mIsMultiSelect) {
                                mAdapter!!.setSelectedItems(ArrayList())
                                mIsMultiSelect = true
                                if (mActionMode == null) {
                                    mActionMode = activity!!.startActionMode(mActionModeCallback)
                                }
                            }
                            multiSelect(position)
                        }
                    })
            )
        }
        return view
    }

    private fun refreshCurrentLabel() {
        if (labelsViewModel.crtExtendedLabel.value != null && mAdapter != null) {
            when (labelsViewModel.crtExtendedLabel.value!!.title) {
                getString(R.string.label_all) -> {
                    sessionsLiveDataAll.observe(viewLifecycleOwner) { sessions: List<Session> ->
                        sessionsLiveDataUnlabeled.removeObservers(this)
                        sessionsLiveDataCrtLabel.removeObservers(this)
                        mAdapter!!.setData(sessions)
                        mSessions = sessions
                        updateRecyclerViewVisibility()
                    }
                }
                "unlabeled" -> {
                    sessionsLiveDataUnlabeled.observe(
                        viewLifecycleOwner
                    ) { sessions: List<Session> ->
                        sessionsLiveDataAll.removeObservers(this)
                        sessionsLiveDataCrtLabel.removeObservers(this)
                        mAdapter!!.setData(sessions)
                        mSessions = sessions
                        updateRecyclerViewVisibility()
                    }
                }
                else -> {
                    sessionsLiveDataCrtLabel =
                        sessionViewModel.getSessions(labelsViewModel.crtExtendedLabel.value!!.title)
                    sessionsLiveDataCrtLabel.observe(
                        viewLifecycleOwner
                    ) { sessions: List<Session> ->
                        sessionsLiveDataAll.removeObservers(this)
                        sessionsLiveDataUnlabeled.removeObservers(this)
                        mAdapter!!.setData(sessions)
                        mSessions = sessions
                        updateRecyclerViewVisibility()
                    }
                }
            }
        }
    }

    private fun multiSelect(position: Int) {
        val s = mAdapter!!.mEntries[position]
        if (mActionMode != null) {
            if (mSelectedEntries.contains(s.id)) {
                mSelectedEntries.remove(s.id)
            } else {
                mSelectedEntries.add(s.id)
            }
            when {
                mSelectedEntries.size == 1 -> {
                    mMenu!!.getItem(0).setIcon(R.drawable.ic_edit)
                    mActionMode!!.title = mSelectedEntries.size.toString()
                }
                mSelectedEntries.size > 1 -> {
                    mMenu!!.getItem(0).setIcon(R.drawable.ic_label)
                    mActionMode!!.title = mSelectedEntries.size.toString()
                }
                else -> {
                    mActionMode!!.title = ""
                    mActionMode!!.finish()
                }
            }
            mAdapter!!.setSelectedItems(mSelectedEntries)

            // hack bellow to avoid multiple dialogs because of observe
            if (mSelectedEntries.size == 1) {
                val sessionId = mAdapter!!.mSelectedEntries[0]
                sessionViewModel.getSession(sessionId).observe(
                    this@AllSessionsFragment
                ) { session: Session? -> mSessionToEdit = session }
            }
        }
    }

    private fun deleteSessions() {
        for (i in mAdapter!!.mSelectedEntries) {
            sessionViewModel.deleteSession(i)
        }
        mAdapter!!.mSelectedEntries.clear()
        if (mActionMode != null) {
            mActionMode!!.finish()
        }
    }

    private fun selectAll() {
        mSelectedEntries.clear()
        for (i in mSessions.indices) {
            mSelectedEntries.add(i, mSessions[i].id)
        }
        when {
            mSelectedEntries.size == 1 -> {
                mMenu!!.getItem(0).setIcon(R.drawable.ic_edit)
                mActionMode!!.title = mSelectedEntries.size.toString()
                mAdapter!!.setSelectedItems(mSelectedEntries)
            }
            mSelectedEntries.size > 1 -> {
                mMenu!!.getItem(0).setIcon(R.drawable.ic_label)
                mActionMode!!.title = mSelectedEntries.size.toString()
                mAdapter!!.setSelectedItems(mSelectedEntries)
            }
            else -> {
                mActionMode!!.title = ""
                mActionMode!!.finish()
            }
        }
    }

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            mMenu = menu
            inflater.inflate(R.menu.menu_all_entries_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val fragmentManager = activity!!.supportFragmentManager
            when (item.itemId) {
                R.id.action_edit -> if (mSelectedEntries.size > 1) {
                    SelectLabelDialog.newInstance(
                        this@AllSessionsFragment,
                        "", false
                    )
                        .show(fragmentManager, StatisticsActivity.DIALOG_SELECT_LABEL_TAG)
                } else if (mSessionToEdit != null) {
                    val newFragment = AddEditEntryDialog.newInstance(mSessionToEdit)
                    newFragment.show(fragmentManager, StatisticsActivity.DIALOG_ADD_ENTRY_TAG)
                    mActionMode!!.finish()
                }
                R.id.action_select_all -> selectAll()
                R.id.action_delete -> AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_selected_entries)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> deleteSessions() }
                    .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                    .show()
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
            mIsMultiSelect = false
            mSelectedEntries = ArrayList()
            mAdapter!!.setSelectedItems(ArrayList())
        }
    }

    override fun onPause() {
        super.onPause()
        if (mActionMode != null) {
            mActionMode!!.finish()
        }
    }

    private fun updateRecyclerViewVisibility() {
        lifecycleScope.launch {
            delay(100)
            mProgressBar!!.visibility = View.GONE
            if (mSessions.isEmpty()) {
                mRecyclerView!!.visibility = View.GONE
                mEmptyState!!.visibility = View.VISIBLE
            } else {
                mRecyclerView!!.visibility = View.VISIBLE
                mEmptyState!!.visibility = View.GONE
            }
        }
    }

    override fun onLabelSelected(label: Label) {
        val title = if (label.title == "unlabeled") null else label.title
        for (i in mSelectedEntries) {
            sessionViewModel.editLabel(i, title)
        }
        if (mActionMode != null) {
            mActionMode!!.finish()
        }
    }
}