package com.apps.adrcotfas.goodtime.main;

import android.content.Intent;

public interface IBillingHelper {
    public void refresh();
    public void release();
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data);
}
