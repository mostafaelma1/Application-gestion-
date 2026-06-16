import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Optional: read a default Anthropic API key from local.properties so it is not
// hard-coded in source. The user can also enter / override it in the Settings screen.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val defaultApiKey: String = (localProps.getProperty("ANTHROPIC_API_KEY") ?: "")

android {
    namespace = "com.candlevision.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.candlevision.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Injected so the app can ship with an optional baked-in key.
        buildConfigField("String", "DEFAULT_ANTHROPIC_API_KEY", "\"$defaultApiKey\"")
        // The vision model used to analyse the chart screenshots.
        buildConfigField("String", "CLAUDE_MODEL", "\"claude-sonnet-4-6\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle + coroutines for running the network call off the main thread.
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room — local SQLite persistence for the analysis history.
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // OkHttp — talks to the Anthropic Messages API.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
