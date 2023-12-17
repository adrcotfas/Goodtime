plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
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
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.bundles.shared.commonTest)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.bundles.shared.androidTest)
            }
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }

    //TODO: remove this when expect/actual become stable; See https://youtrack.jetbrains.com/issue/KT-61573
    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

android {
    namespace = "com.apps.adrcotfas.goodtime.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

//TODO:
tasks.register("testClasses") { }
