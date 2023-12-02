package com.apps.adrcotfas.goodtime

import android.app.Application
import android.content.Context
import com.apps.adrcotfas.goodtime.di.insertKoin
import org.koin.dsl.module

class GoodtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        insertKoin(
            module {
                single<Context> { this@GoodtimeApplication }
            }
        )
    }
}