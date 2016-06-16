package com.apps.adrcotfas.goodtime.about;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.util.IabHelper;
import com.apps.adrcotfas.goodtime.util.IabResult;
import com.apps.adrcotfas.goodtime.util.Inventory;
import com.apps.adrcotfas.goodtime.util.Purchase;

import java.util.ArrayList;
import java.util.List;

public class SupportTheDeveloperActivity extends AppCompatActivity{

    private static final String TAG = "Donations";
    IabHelper mHelper;

    // SKUs for the donations
    static final String SKU_1_DOLLAR = "1_dollar";
    static final String SKU_5_DOLLARS = "5_dollars";
    static final String SKU_10_DOLLARS = "10_dollars";
    static final String SKU_25_DOLLARS = "25_dollars";
    static final String SKU_50_DOLLARS = "50_dollars";


    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener  mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }

            Toast.makeText(SupportTheDeveloperActivity.this, "Purchase successful.",
                    Toast.LENGTH_LONG).show();

            Log.d(TAG, "Consume the purchase.");
            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                Toast.makeText(SupportTheDeveloperActivity.this, "Consume the purchase",
                        Toast.LENGTH_LONG).show();

                Toast.makeText(SupportTheDeveloperActivity.this, "Consume the purchase",
                        Toast.LENGTH_LONG).show();

                Toast.makeText(SupportTheDeveloperActivity.this, "Consume the purchase",
                        Toast.LENGTH_LONG).show();

            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error consuming gas. Another async operation in progress.");
                return;
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Toast.makeText(SupportTheDeveloperActivity.this, "Consumption finished. Purchase: " + purchase + ", result: " + result,
                    Toast.LENGTH_LONG).show();

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Toast.makeText(SupportTheDeveloperActivity.this, "Consumption successful..",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(SupportTheDeveloperActivity.this, "Error while consuming: " + result,
                        Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "End consumption flow.");
        }
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase purchase1 = inventory.getPurchase(SKU_1_DOLLAR);
            if (purchase1 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_1_DOLLAR), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

            Purchase purchase5 = inventory.getPurchase(SKU_5_DOLLARS);
            if (purchase5 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_5_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

            Purchase purchase10 = inventory.getPurchase(SKU_10_DOLLARS);
            if (purchase10 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_10_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }
            Purchase purchase25 = inventory.getPurchase(SKU_25_DOLLARS);
            if (purchase25 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_25_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

            Purchase purchase50 = inventory.getPurchase(SKU_50_DOLLARS);
            if (purchase50 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_50_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.support_the_developer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.supportToolbar);
        if (toolbar != null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

                List<String> skulist = new ArrayList<String>();
                skulist.add("1_dollar");
                skulist.add("5_dollars");
                skulist.add("10_dollars");
                skulist.add("25_dollars");
                skulist.add("50_dollars");

                try {
                    mHelper.queryInventoryAsync(false, skulist, null, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.donations, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        assert spinner != null;
        spinner.setAdapter(adapter);

        Button button = (Button) findViewById(R.id.donate);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mHelper.flagEndAsync();
            String selectedValue = spinner.getSelectedItem().toString();
                if (selectedValue.equals("$1")){
                    try {
                        mHelper.launchPurchaseFlow(SupportTheDeveloperActivity.this, SKU_1_DOLLAR, 667,
                                mPurchaseFinishedListener, "");
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
                else if (selectedValue.equals("$5")){
                    try {
                        mHelper.launchPurchaseFlow(SupportTheDeveloperActivity.this, SKU_5_DOLLARS, 667,
                                mPurchaseFinishedListener, "");
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
                else if (selectedValue.equals("$10")){
                    try {
                        mHelper.launchPurchaseFlow(SupportTheDeveloperActivity.this, SKU_10_DOLLARS, 667,
                                mPurchaseFinishedListener, "");
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
                else if (selectedValue.equals("$25")){
                    try {
                        mHelper.launchPurchaseFlow(SupportTheDeveloperActivity.this, SKU_25_DOLLARS, 667,
                                mPurchaseFinishedListener, "");
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
                else if (selectedValue.equals("$50")){
                    try {
                        mHelper.launchPurchaseFlow(SupportTheDeveloperActivity.this, SKU_50_DOLLARS, 667,
                                mPurchaseFinishedListener, "");
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    void complain(String message) {

        Toast.makeText(SupportTheDeveloperActivity.this, "**** TrivialDrive Error: " + message,
                Toast.LENGTH_LONG).show();
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


}
