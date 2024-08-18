import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.apps.adrcotfas.goodtime.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            isStatic = false
            linkerOpts("-lsqlite3")
            export(libs.touchlab.kermit.simple)
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.okio)
            api(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.lifecycle.viewmodel)
            api(libs.touchlab.kermit)
        }
        commonTest.dependencies {
            implementation(libs.bundles.shared.commonTest)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }
        getByName("androidUnitTest") {
            dependencies {
                implementation(libs.bundles.shared.androidTest)
            }
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
    // https://kotlinlang.org/docs/multiplatform-expect-actual.html#expected-and-actual-classes
    // To suppress this warning about usage of expected and actual classes
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.apps.adrcotfas.goodtime.data.local")
            version = 6
            verifyMigrations = true
        }
    }
    linkSqlite = true
}

//TODO:
tasks.register("testClasses") { }
