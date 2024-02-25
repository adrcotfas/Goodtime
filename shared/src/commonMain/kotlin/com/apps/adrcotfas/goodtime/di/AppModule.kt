package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.apps.adrcotfas.goodtime.bl.BreakBudgetHandler
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepositoryImpl
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.FinishedSessionsHandler
import com.apps.adrcotfas.goodtime.bl.StreakAndLongBreakHandler
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimeProviderImpl
import com.apps.adrcotfas.goodtime.bl.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module


private val coroutineScopeModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

fun insertKoin(appModule: Module): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule,
            coroutineScopeModule,
            platformModule,
            coreModule
        )
    }

    val timerManager: TimerManager = koinApplication.koin.get()
    val applicationCoroutineScope: CoroutineScope = koinApplication.koin.get()
    applicationCoroutineScope.launch {
        timerManager.init()
    }

    return koinApplication
}

expect fun isDebug(): Boolean
private val String.withPrefixIfDebug
    get() = if (isDebug()) "### $this" else this

expect val platformModule: Module

private val coreModule = module {
    val baseLogger =
        Logger(
            config = StaticConfig(
                logWriterList = listOf(platformLogWriter()),
                minSeverity = if (isDebug()) Severity.Verbose else Severity.Info
            ),
            tag = "Goodtime".withPrefixIfDebug
        )
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag.withPrefixIfDebug) else baseLogger }

    single<LocalDataRepository> {
        LocalDataRepositoryImpl(Database(driver = get<DatabaseDriverFactory>().create()))
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get<DataStore<Preferences>>(named(SETTINGS_NAME)),
            getWith(SettingsRepository::class.simpleName)
        )
    }
    single<TimeProvider> {
        TimeProviderImpl()
    }

    single<FinishedSessionsHandler> {
        FinishedSessionsHandler(
            get<CoroutineScope>(),
            get<LocalDataRepository>(),
            getWith(FinishedSessionsHandler::class.simpleName)
        )
    }

    single<StreakAndLongBreakHandler> {
        StreakAndLongBreakHandler(get<CoroutineScope>(), get<SettingsRepository>())
    }

    single<BreakBudgetHandler> {
        BreakBudgetHandler(get<CoroutineScope>(), get<SettingsRepository>())
    }

    single<TimerManager> {
        TimerManager(
            get<LocalDataRepository>(),
            get<SettingsRepository>(),
            get<List<EventListener>>(),
            get<TimeProvider>(),
            get<FinishedSessionsHandler>(),
            get<StreakAndLongBreakHandler>(),
            get<BreakBudgetHandler>(),
            getWith(TimerManager::class.simpleName)
        )
    }
}

internal const val SETTINGS_NAME = "productivity_settings.preferences"
internal const val SETTINGS_FILE_NAME = SETTINGS_NAME + "_pb"

internal fun getDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
}

inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }