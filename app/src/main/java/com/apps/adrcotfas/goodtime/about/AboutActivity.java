package com.apps.adrcotfas.goodtime.about;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.ProductTourActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.util.DeviceInfo;
import com.apps.adrcotfas.goodtime.util.IabHelper;
import com.apps.adrcotfas.goodtime.util.IabResult;
import com.apps.adrcotfas.goodtime.util.Inventory;
import com.apps.adrcotfas.goodtime.util.Purchase;

public class AboutActivity extends AppCompatActivity {

    // SKUs for the donations
    static final String SKU_1_DOLLAR = "1_dollar";
    static final String SKU_3_DOLLARS = "3_dollars";
    static final String SKU_5_DOLLARS = "5_dollars";
    String TAG = "[AboutActivity]";
    IabHelper mHelper;
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
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
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                return;
            }

            Log.d(TAG, "Purchase successful.");
            Toast.makeText(AboutActivity.this, "Thank you for your donation!", Toast.LENGTH_LONG).show();
            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                Log.e(TAG, "Error consuming the purchase. Another async operation in progress.");
            }
        }
    };
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
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
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_3_DOLLARS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming the purchase.");
                }
            }

            Purchase purchase5 = inventory.getPurchase(SKU_5_DOLLARS);
            if (purchase5 != null) {
                Log.d(TAG, "We have gas. Consuming it.");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        getFragmentManager().beginTransaction()
                .replace(R.id.about_content, new AboutFragment())
                .commit();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about);

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.aboutToolbar);
            if (toolbar != null) {
                ((AboutActivity) getActivity()).setSupportActionBar(toolbar);

                ((AboutActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
                ((AboutActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Preference appVersion = findPreference("about_version");
            appVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://github.com/adrcotfas");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });
            Preference supportTheDeveloper = findPreference("about_pro");
            supportTheDeveloper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    final CharSequence[] items = getResources().getStringArray(R.array.donations);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("Select the amount. VAT is not included.");
                    builder.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (items[item].equals("$1")) {
                                try {
                                    ((AboutActivity) getActivity()).getIabHelper().launchPurchaseFlow(getActivity(), SKU_1_DOLLAR, 667,
                                            ((AboutActivity) getActivity()).getPurchaseListener(), "");
                                } catch (IabHelper.IabAsyncInProgressException e) {
                                    e.printStackTrace();
                                }
                            } else if (items[item].equals("$3")) {
                                try {
                                    ((AboutActivity) getActivity()).getIabHelper().launchPurchaseFlow(getActivity(), SKU_3_DOLLARS, 667,
                                            ((AboutActivity) getActivity()).getPurchaseListener(), "");
                                } catch (IabHelper.IabAsyncInProgressException e) {
                                    e.printStackTrace();
                                }
                            } else if (items[item].equals("$5")) {
                                try {
                                    ((AboutActivity) getActivity()).getIabHelper().launchPurchaseFlow(getActivity(), SKU_5_DOLLARS, 667,
                                            ((AboutActivity) getActivity()).getPurchaseListener(), "");
                                } catch (IabHelper.IabAsyncInProgressException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    builder.show();
                    return true;
                }
            });

            Preference rateThisApp = findPreference("about_rate");
            rateThisApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final String appPackageName = getActivity().getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    return true;
                }
            });

            Preference feedback = findPreference("about_feedback");
            feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent email = new Intent(Intent.ACTION_SENDTO);
                    email.setData(new Uri.Builder().scheme("mailto").build());
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"adrcotfas@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime] Feedback");
                    email.putExtra(Intent.EXTRA_TEXT, "\nMy device info: \n" + DeviceInfo.getDeviceInfo() + "\nFeedback:" + "\n");
                    try {
                        startActivity(Intent.createChooser(email, "Send feedback"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText((getActivity()), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            Preference licences = findPreference("about_licences");
            licences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), LicencesActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            Preference productTour = findPreference("about_product_tour");
            productTour.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), ProductTourActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
}
