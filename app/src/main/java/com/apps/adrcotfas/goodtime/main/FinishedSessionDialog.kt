/*
 * Copyright 2016-2020 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.main

import com.apps.adrcotfas.goodtime.bl.SessionType
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.apps.adrcotfas.goodtime.R
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import java.lang.ClassCastException

class FinishedSessionDialog : DialogFragment() {
    interface Listener {
        fun onFinishedSessionDialogPositiveButtonClick(sessionType: SessionType)
        fun onFinishedSessionDialogNeutralButtonClick(sessionType: SessionType)
    }

    private var listener: Listener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            activity as Listener?
        } catch (e: ClassCastException) {
            throw ClassCastException("hosting activity must implement FinishedSessionDialog::Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val builder = AlertDialog.Builder(requireContext())
        val viewModel: TimerActivityViewModel by activityViewModels()
        val sessionType = viewModel.dialogPendingType
        if (sessionType === SessionType.WORK) {
            builder.setTitle(R.string.action_finished_session)
                .setPositiveButton(R.string.action_start_break) { _, _ ->
                    listener!!.onFinishedSessionDialogPositiveButtonClick(
                        sessionType
                    )
                }
                .setNeutralButton(R.string.dialog_close) { _, _ ->
                    listener!!.onFinishedSessionDialogNeutralButtonClick(
                        sessionType
                    )
                }
        } else {
            builder.setTitle(R.string.action_finished_break)
                .setPositiveButton(R.string.action_start_work) { _, _ ->
                    listener!!.onFinishedSessionDialogPositiveButtonClick(
                        sessionType
                    )
                }
                .setNeutralButton(android.R.string.cancel) { _, _ ->
                    listener!!.onFinishedSessionDialogNeutralButtonClick(
                        sessionType
                    )
                }
        }
        val d: Dialog = builder
            .setCancelable(false)
            .create()
        d.setCanceledOnTouchOutside(false)
        return d
    }

    companion object {
        fun newInstance(listener: Listener?): FinishedSessionDialog {
            val dialog = FinishedSessionDialog()
            dialog.listener = listener
            return dialog
        }
    }
}