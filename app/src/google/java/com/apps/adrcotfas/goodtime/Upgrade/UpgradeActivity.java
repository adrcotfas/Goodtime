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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityUpgradeBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE;
import static com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE;

public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    private boolean readyToPurchase = false;
    private Button buy;
    private BillingProcessor mBillingProcessor;
    private ProgressBar mProgressBar;
    private ScrollView mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        ActivityUpgradeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);

        buy = binding.buttonPro;
        buy.setOnClickListener(v -> {
            if (!readyToPurchase) {
                Toast.makeText(
                        UpgradeActivity.this,
                        "Billing not initialized",
                        Toast.LENGTH_LONG).show();
            }
            else {
                mBillingProcessor.purchase(UpgradeActivity.this, Constants.sku);
            }
        });
        mProgressBar = binding.progressBar;
        mProgressBar.setVisibility(View.VISIBLE);
        mContent = binding.content;
        mContent.setVisibility(View.GONE);

        String checkMark = getString(R.string.check_mark) + " ";

        StringBuilder content = new StringBuilder();
        for (String s : getResources().getStringArray(R.array.update_pro)) {
            content.append(checkMark).append(s).append("\n");
        }

        binding.contentPro.setText(content);

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mBillingProcessor = BillingProcessor.newBillingProcessor(this, getString(R.string.licence_key), getString(R.string.merchant_id),  this);
        mBillingProcessor.initialize();

        if(!BillingProcessor.isIabServiceAvailable(this)) {
            Toast.makeText(
                    UpgradeActivity.this,
                    "In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        PreferenceHelper.setPro(true);
        buy.setEnabled(false);
        Toast.makeText(UpgradeActivity.this, getString(R.string.upgrade_enjoy), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        boolean found = false;
        for(String sku : mBillingProcessor.listOwnedProducts()) {
            if (sku.equals(Constants.sku)) {
                buy.setEnabled(false);
                found = true;
            }
        }
        PreferenceHelper.setPro(found);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        // do nothing here
        switch (errorCode) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
                Toast.makeText(
                    UpgradeActivity.this,
                    "Billing API version is not supported for the type requested",
                    Toast.LENGTH_LONG).show();
                break;
            case BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
                Toast.makeText(
                        UpgradeActivity.this,
                        "Network connection is down",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onBillingInitialized() {
        readyToPurchase = true;
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
        if (mBillingProcessor.isPurchased(Constants.sku)) {
            buy.setVisibility(View.GONE);
            PreferenceHelper.setPro(true);
        } else {
            SkuDetails details = mBillingProcessor.getPurchaseListingDetails(Constants.sku);
            if (details != null) {
                buy.setText(details.priceText);
            }
            PreferenceHelper.setPro(false);
        }

        mProgressBar.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
    }
}
