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
import android.os.Bundle;
import android.widget.Button;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityUpgradeBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    private static String sku = "android.test.purchased";
    private boolean readyToPurchase = false;
    private Button buy;
    private BillingProcessor bp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        bp = BillingProcessor.newBillingProcessor(this, getString(R.string.licence_key), getString(R.string.merchant_id),  this);
        bp.initialize();

        ActivityUpgradeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        buy = binding.buttonPro;
        buy.setOnClickListener(v -> {
            if (!readyToPurchase) {
                // ("Billing not initialized.");
            }
            else {
                bp.purchase(UpgradeActivity.this, sku);
            }
        });

        binding.buttonConsume.setOnClickListener(v -> bp.consumePurchase(sku));
        if (bp.isPurchased(sku)) {
            binding.buttonPro.setBackgroundColor(getResources().getColor(R.color.red));
            binding.buttonPro.setText(R.string.about_version);
        }

        if(!BillingProcessor.isIabServiceAvailable(this)) {
            // ("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String checkMark = getString(R.string.check_mark) + " ";

        StringBuilder content = new StringBuilder();
        for (String s : getResources().getStringArray(R.array.update_pro)) {
            content.append(checkMark).append(s).append("\n");
        }

        binding.contentPro.setText(content);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        // activate pro in preferences
        // deactivate buy button
    }

    @Override
    public void onPurchaseHistoryRestored() {
        for(String sku : bp.listOwnedProducts()) {
            if (sku.equals(UpgradeActivity.sku)) {
                // activate PRO in preferences
                // deactivate buy button
            }
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        // show an error on the screen
        // log
    }

    @Override
    public void onBillingInitialized() {
        readyToPurchase = true;
        bp.loadOwnedPurchasesFromGoogle();
        SkuDetails details = bp.getPurchaseListingDetails(sku);
        buy.setText(details.priceText);
    }
}
