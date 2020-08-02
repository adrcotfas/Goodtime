package com.apps.adrcotfas.goodtime.upgrade;

import android.content.Context;
import android.content.Intent;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.apps.adrcotfas.goodtime.Main.IBillingHelper;
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;

import androidx.annotation.NonNull;

public class BillingHelper implements IBillingHelper, BillingProcessor.IBillingHandler  {

    private BillingProcessor mBillingProcessor;

    public BillingHelper(Context context) {
        mBillingProcessor = BillingProcessor.newBillingProcessor(
                context,
                context.getString(R.string.licence_key),
                context.getString(R.string.merchant_id),  this);
        mBillingProcessor.initialize();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        // do nothing here
    }

    @Override
    public void onPurchaseHistoryRestored() {
        boolean found = false;
        for(String sku : mBillingProcessor.listOwnedProducts()) {
            if (sku.equals(Constants.sku)) {
                found = true;
                break;
            }
        }
        PreferenceHelper.setPro(found);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        // do nothing here
    }

    @Override
    public void onBillingInitialized() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
        if (mBillingProcessor.isPurchased(Constants.sku)) {
            PreferenceHelper.setPro(true);
        }
    }

    @Override
    public void refresh() {
        if (mBillingProcessor != null) {
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
        }
    }

    @Override
    public void release() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBillingProcessor != null) {
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
            if (mBillingProcessor.isPurchased(Constants.sku)) {
                PreferenceHelper.setPro(true);
            }
            return mBillingProcessor.handleActivityResult(requestCode, resultCode, data);
        } else {
            return false;
        }
    }
}
