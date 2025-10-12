plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("org.sonarqube") version "6.3.1.5724"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.carcollection"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carcollection"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Ensure the Firebase BoM is declared FIRST to manage versions consistently
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // Keep this at the top of your Firebase dependencies!

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx") // ADDED: Firebase Authentication with Kotlin extensions

    // Cloud Firestore (Consolidated and managed by BoM)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Analytics (Using KTX version for consistency and BoM management)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Kotlin Coroutines for Play Services (Provides .await() for Firebase Tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1") // ADDED: Or check for the very latest version

    // Your existing Crashlytics buildtools (Note: this often works with a separate Crashlytics SDK)
    implementation(libs.firebase.crashlytics.buildtools)

    // --- Your other existing dependencies ---

    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.fragment.ktx)
    // implementation(libs.firebase.firestore.ktx) // REMOVED: Redundant
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Keep your serialization version
    val roomVersion = "2.7.1" // Use the latest stable version
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion") // Use kapt
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.compose.material:material-icons-extended")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-android:2.3.4") // importante para Android
    implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-gson:2.3.4")
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation(libs.gson)
    implementation(libs.androidx.navigation.compose) // You have this twice, consider consolidating if not intended
    // implementation(libs.androidx.navigation.compose) // Removed potential duplicate
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.navigation.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.coil-kt:coil-gif:2.5.0")
}