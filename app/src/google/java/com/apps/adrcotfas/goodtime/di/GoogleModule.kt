package com.apps.adrcotfas.goodtime.di

import android.content.Context
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.upgrade.BillingHelper
import com.apps.adrcotfas.goodtime.upgrade.LICENCE_KEY
import com.apps.adrcotfas.goodtime.upgrade.SKU
import com.limerse.iap.IapConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object GoogleModule {

    @Provides
    @ActivityScoped
    fun provideIapConnector(@ApplicationContext appContext: Context) = IapConnector(
        context = appContext,
        nonConsumableKeys = arrayListOf(SKU),
        key = LICENCE_KEY,
        enableLogging = true
    )

    @Provides
    @ActivityScoped
    fun provideBillingHelper(iapConnector: IapConnector, preferenceHelper: PreferenceHelper) =
        BillingHelper(iapConnector, preferenceHelper)
}
