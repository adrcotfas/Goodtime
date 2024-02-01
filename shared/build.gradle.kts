plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
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
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.bundles.shared.commonTest)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.core.ktx)
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

    //TODO: remove this when expect/actual become stable; See https://youtrack.jetbrains.com/issue/KT-61573
    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    cocoapods {
        summary = "Productivity app for focus and time management"
        homepage = "https://github.com/adrcotfas/goodtime"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = false // SwiftUI preview requires dynamic framework
            linkerOpts("-lsqlite3")
        }
        extraSpecAttributes["swift_version"] = "\"5.0\"" // <- SKIE Needs this!
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
