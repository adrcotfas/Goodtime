/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apps.adrcotfas.goodtime.di

import android.content.Context
import com.apps.adrcotfas.goodtime.BillingRepository
import com.apps.adrcotfas.goodtime.BillingRepository.Companion.INAPP_SKUS
import com.apps.adrcotfas.goodtime.BillingRepository.Companion.LICENCE_KEY
import com.apps.adrcotfas.goodtime.billing.BillingDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleModule {

    @DelicateCoroutinesApi
    @Provides
    @Singleton
    fun provideBillingDataSource(
        @ApplicationContext context: Context) = BillingDataSource.getInstance(context, GlobalScope, LICENCE_KEY, INAPP_SKUS)

    @DelicateCoroutinesApi
    @Provides
    @Singleton
    fun provideBillingRepository(dataSource: BillingDataSource) = BillingRepository(dataSource, GlobalScope)
}
