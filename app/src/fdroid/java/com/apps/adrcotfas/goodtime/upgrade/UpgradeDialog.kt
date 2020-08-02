package com.apps.adrcotfas.goodtime.upgrade

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.databinding.DialogUpgradeBinding

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
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme("https").authority("www.paypal.com").path("cgi-bin/webscr")
        uriBuilder.appendQueryParameter("cmd", "_donations")
        uriBuilder.appendQueryParameter("business", PAYPAL_USER)
        uriBuilder.appendQueryParameter("lc", "US")
        uriBuilder.appendQueryParameter("item_name", "${getString(R.string.app_name_long)} : ${getString(R.string.support_the_developer)}")
        uriBuilder.appendQueryParameter("no_note", "1")
        uriBuilder.appendQueryParameter("no_shipping", "1")
        uriBuilder.appendQueryParameter("currency_code", PAYPAL_CURRENCY_CODE)
        val payPalUri = uriBuilder.build()
        val viewIntent = Intent(Intent.ACTION_VIEW, payPalUri)
        val title: String = resources.getString(org.sufficientlysecure.donations.R.string.donations__paypal)
        val chooser = Intent.createChooser(viewIntent, title)
        if (viewIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(requireContext(),
                    getString(org.sufficientlysecure.donations.R.string.donations__alert_dialog_title),
                    Toast.LENGTH_LONG).show()
        }
        dismiss()
    }

    companion object {
        private const val PAYPAL_USER = "adrcotfas@gmail.com"
        private const val PAYPAL_CURRENCY_CODE = "EUR"

        fun showNewInstance(fragmentManager: FragmentManager) {
            val dialog = UpgradeDialog()
            dialog.show(fragmentManager, "")
        }
    }
}