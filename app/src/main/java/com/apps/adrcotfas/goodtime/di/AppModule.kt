package com.apps.adrcotfas.goodtime.di

import android.content.Context
import com.apps.adrcotfas.goodtime.bl.CurrentSessionManager
import com.apps.adrcotfas.goodtime.bl.NotificationHelper
import com.apps.adrcotfas.goodtime.bl.RingtoneAndVibrationPlayer
import com.apps.adrcotfas.goodtime.database.AppDatabase
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper
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

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context) = NotificationHelper(context)

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context) = AppDatabase.getDatabase(context)
}