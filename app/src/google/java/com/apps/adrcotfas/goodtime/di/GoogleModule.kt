package com.apps.adrcotfas.goodtime.di

import android.content.Context
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.upgrade.BillingHelper
import com.limerse.iap.IapConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleModule {
    private const val LICENCE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgH+Nj4oEbJKEnrds3qaDcdjti0hnL1hlYsOoX5hVNUs4CpTzVmiAtO3LHwLGJzvtDmagsszKgVFn3SmVeA7y+GS93I6FwsCEmXNGdaCJW4TftLqSxT9Q4Qn8R8hWk3OXgo1ZF2FxGuicwq9zt4W+6pW7QMhpoBA0DyCLhoCulINVTkEKBBWeCS4CDkhXrnXCoAbhmYn2R7Ifhn7voy1YR9Vr/G9tCHzvLM1k4bntyOebxdMwPy49Dsrzam1hgPhzmEMqwolchLx95DFXVfHcWSFtBpZwR4sPFhXny5CQ255CruCdQd8L5CHdRhrHyNkzBVrwoYg8WWZUQ3Ijcu2e5wIDAQAB"
    private const val SKU = "upgraded_version"

    @Provides
    @Singleton
    fun provideIapConnector(@ApplicationContext appContext: Context) = IapConnector(
        context = appContext,
        nonConsumableKeys = arrayListOf(SKU),
        key = LICENCE_KEY,
        enableLogging = true
    )

    @Provides
    @Singleton
    fun provideBillingHelper(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) = BillingHelper(context, preferenceHelper)
}
