plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    explicitApi()

    androidLibrary {
        namespace = "com.kptrsz.mascot.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":mascot-core"))
            api(libs.compose.runtime)
            implementation(libs.compose.foundation)
            api(libs.compose.ui)
        }
    }
}
