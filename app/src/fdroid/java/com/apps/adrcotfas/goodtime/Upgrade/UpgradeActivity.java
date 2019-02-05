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

package com.apps.adrcotfas.goodtime.Upgrade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.databinding.ActivityDonateBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class UpgradeActivity extends AppCompatActivity {

    private static final String PAYPAL_USER = "adrcotfas@gmail.com";
    private static final String PAYPAL_CURRENCY_CODE = "EUR";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDonateBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_donate);
        binding.buttonPro.setOnClickListener(this::donatePayPalOnClick);
        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void donatePayPalOnClick(View view) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https").authority("www.paypal.com").path("cgi-bin/webscr");
        uriBuilder.appendQueryParameter("cmd", "_donations");

        uriBuilder.appendQueryParameter("business", PAYPAL_USER);
        uriBuilder.appendQueryParameter("lc", "US");
        uriBuilder.appendQueryParameter("item_name", getString(R.string.donate));
        uriBuilder.appendQueryParameter("no_note", "1");
        uriBuilder.appendQueryParameter("no_shipping", "1");
        uriBuilder.appendQueryParameter("currency_code", PAYPAL_CURRENCY_CODE);
        Uri payPalUri = uriBuilder.build();

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, payPalUri);
        String title = getResources().getString(org.sufficientlysecure.donations.R.string.donations__paypal);
        Intent chooser = Intent.createChooser(viewIntent, title);
        if (viewIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(UpgradeActivity.this,
                    getString(org.sufficientlysecure.donations.R.string.donations__alert_dialog_title),
                    Toast.LENGTH_LONG).show();
        }
    }
}
