package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.util.IabHelper;
import com.apps.adrcotfas.goodtime.util.IabResult;
import com.apps.adrcotfas.goodtime.util.Inventory;
import com.apps.adrcotfas.goodtime.util.Purchase;

public class DonationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donations_layout);
        setupIabHelper();
        setupButtons();
    }

    private static final String TAG = "DonationsActivity";

    static final String SKU_1_DOLLAR = "1_dollar";
    static final String SKU_3_DOLLARS = "3_dollars";
    static final String SKU_5_DOLLARS = "5_dollars";

    IabHelper mHelper;
    // Called when consumption is complete
    final IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                Log.e(TAG, "Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };

    // Callback for when a purchase is finished
    final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                return;
            }

            Log.d(TAG, "Purchase successful.");
            Toast.makeText(DonationsActivity.this, "Thank you for your donation!", Toast.LENGTH_LONG).show();
            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                Log.e(TAG, "Error consuming the purchase. Another async operation in progress.");
            }
        }
    };

    final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                return;
            }
            Log.d(TAG, "Query inventory was successful.");
            Purchase purchase1 = inventory.getPurchase(SKU_1_DOLLAR);
            if (purchase1 != null) {
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_1_DOLLAR), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming the purchase.");
                }
            }

            Purchase purchase3 = inventory.getPurchase(SKU_3_DOLLARS);
            if (purchase3 != null) {
                Log.d(TAG, "Consume purchase.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_3_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming the purchase.");
                }
            }

            Purchase purchase5 = inventory.getPurchase(SKU_5_DOLLARS);
            if (purchase5 != null) {
                Log.d(TAG, "Consume purchase.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_5_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming the purchase.");
                }
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    protected IabHelper getIabHelper() {
        return mHelper;
    }

    protected IabHelper.OnIabPurchaseFinishedListener getPurchaseListener() {
        return mPurchaseFinishedListener;
    }

    private void setupIabHelper() {
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgH+Nj4oEbJKEnrds3qaDcdjti0hnL1hlYsOoX5hVNUs4CpTzVmiAtO3LHwLGJzvtDmagsszKgVFn3SmVeA7y+GS93I6FwsCEmXNGdaCJW4TftLqSxT9Q4Qn8R8hWk3OXgo1ZF2FxGuicwq9zt4W+6pW7QMhpoBA0DyCLhoCulINVTkEKBBWeCS4CDkhXrnXCoAbhmYn2R7Ifhn7voy1YR9Vr/G9tCHzvLM1k4bntyOebxdMwPy49Dsrzam1hgPhzmEMqwolchLx95DFXVfHcWSFtBpZwR4sPFhXny5CQ255CruCdQd8L5CHdRhrHyNkzBVrwoYg8WWZUQ3Ijcu2e5wIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(false);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    private void setupButtons() {

        Button donate1 = (Button) findViewById(R.id.donation_1_dollar);
        donate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getIabHelper().launchPurchaseFlow(DonationsActivity.this, SKU_1_DOLLAR, 667,
                            getPurchaseListener(), "");
                } catch (IabHelper.IabAsyncInProgressException | IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(DonationsActivity.this, R.string.about_something_wrong, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button donate3 = (Button) findViewById(R.id.donation_3_dollars);
        donate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getIabHelper().launchPurchaseFlow(DonationsActivity.this, SKU_3_DOLLARS, 667,
                            getPurchaseListener(), "");
                } catch (IabHelper.IabAsyncInProgressException | IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(DonationsActivity.this, R.string.about_something_wrong, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button donate5 = (Button) findViewById(R.id.donation_5_dollars);
        donate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getIabHelper().launchPurchaseFlow(DonationsActivity.this, SKU_5_DOLLARS, 667,
                            getPurchaseListener(), "");
                } catch (IabHelper.IabAsyncInProgressException | IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(DonationsActivity.this, R.string.about_something_wrong, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
