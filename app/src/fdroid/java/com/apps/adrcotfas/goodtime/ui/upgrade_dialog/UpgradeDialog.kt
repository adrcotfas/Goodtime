package com.apps.adrcotfas.goodtime.ui.upgrade_dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.databinding.DialogUpgradeBinding
import com.apps.adrcotfas.goodtime.util.showOnce

class UpgradeDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding : DialogUpgradeBinding = DataBindingUtil.inflate(
                LayoutInflater.from(requireActivity()), R.layout.dialog_upgrade, null, false)
        binding.buttonPaypal.setOnClickListener { donatePayPalOnClick() }
        binding.buttonBitcoin.setOnClickListener{ donateBitcoinOnClick() }
        val builder = AlertDialog.Builder(requireActivity())
                .setView(binding.root)
                .setTitle("")
        return builder.create()
    }

    private fun donateBitcoinOnClick() {
        val url = "https://live.blockcypher.com/btc/address/bc1qte8xf2hvn85s7tl4atqk47r372ktv9d0mn7fu6/"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
        dismiss()
    }

    private fun donatePayPalOnClick() {
        val url = "https://paypal.me/adrcotfas"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
        dismiss()
    }

    companion object {
        fun showNewInstance(fragmentManager: FragmentManager) {
            val dialog = UpgradeDialog()
            dialog.showOnce(fragmentManager, "UpgradeDialog")
        }
    }
}