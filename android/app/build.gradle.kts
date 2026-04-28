import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("ru.ok.tracer") version "1.0.8"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.funnyenglish"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.funnyenglish"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    assetPacks += listOf(":model_asset_pack")

    tracer {
        create("defaultConfig") {
            pluginToken = localProperties.getProperty("tracer.plugin.token", "")
            appToken = localProperties.getProperty("tracer.app.token", "")
            uploadMapping = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":feature:home"))
    implementation(project(":feature:dictionary"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:games"))
    implementation(project(":feature:profile"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.asset.delivery)
    implementation(libs.play.asset.delivery.ktx)

    // AppTracer (RuStore analytics / crash reporting)
    implementation(platform(libs.tracer.platform))
    implementation(libs.tracer.base)
    implementation(libs.tracer.crash.report)
    implementation(libs.tracer.crash.report.native)
    implementation(libs.tracer.heap.dumps)
}
