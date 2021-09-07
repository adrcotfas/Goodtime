package com.apps.adrcotfas.goodtime.di

import android.content.Context
import com.apps.adrcotfas.goodtime.BL.CurrentSessionManager
import com.apps.adrcotfas.goodtime.BL.RingtoneAndVibrationPlayer
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.Settings.reminders.ReminderHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePreferenceHelper(@ApplicationContext context: Context) = PreferenceHelper(context)

    @Provides
    @Singleton
    fun provideRingtoneAndVibrationPlayer(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) =
        RingtoneAndVibrationPlayer(context, preferenceHelper)

    @Provides
    @Singleton
    fun provideCurrentSessionManager(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) = CurrentSessionManager(context, preferenceHelper)

    @Provides
    @Singleton
    fun provideReminderHelper(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) = ReminderHelper(context, preferenceHelper)
}