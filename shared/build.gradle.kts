plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
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
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }

        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
    compileSdk = 34
    defaultConfig {
        minSdk = 26
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
