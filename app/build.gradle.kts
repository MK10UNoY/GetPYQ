import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("kotlin-kapt") // ✅ Required for Annotation Processing
    id("dagger.hilt.android.plugin") // ✅ Required for Hilt
    id("com.google.gms.google-services") // ✅ Required for Firebase
}
android {
    namespace = "com.infomanix.getpyq"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.infomanix.getpyq"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "1.9" // ✅ Force Kotlin 1.9
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    kapt {
        correctErrorTypes = true // ✅ Fix KAPT errors
    }
}

val supabaseVersion = "3.1.3"
val ktor_version = "3.1.1"
dependencies {    implementation(libs.androidx.runtime.livedata)

    // ✅ Android & Compose Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(kotlin("reflect"))

    // ✅ Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)


    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.firebase.common.ktx)

    // ✅ CameraX
    implementation(libs.androidx.camera.core.v130)
    implementation(libs.androidx.camera.camera2.v130)
    implementation(libs.androidx.camera.lifecycle.v130)
    implementation(libs.androidx.camera.view.v130)

    // ✅ Image Processing & UI
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.ucrop) // Crop library
    implementation(libs.androidx.palette.ktx)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.text.recognition)
    implementation(libs.androidx.multidex)

    // ✅ Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ✅ Supabase Integration
    implementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")

    // ✅ Ktor (Networking)
    implementation("io.ktor:ktor-client-android:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-utils:$ktor_version")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    // ✅ Ktor Serialization (Required if using JSON parsing)
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    // ✅ Required for `HttpTimeout`
    implementation("io.ktor:ktor-client-plugins:$ktor_version")
    // ✅ Ktor Logging (Useful for debugging)
    implementation("io.ktor:ktor-client-logging:$ktor_version")

    // ✅ Kotlin & Serialization
    implementation(kotlin("stdlib"))
    implementation("org.slf4j:slf4j-android:1.7.36")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.accompanist.navigation.animation.v0332alpha)
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.ktor") {
            useVersion(ktor_version) // Now matches across all dependencies
        }
    }
}