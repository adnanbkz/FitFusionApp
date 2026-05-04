import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    /*id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")*/
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun configValue(name: String): String =
    providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: System.getenv(name)
        ?: ""

fun escapedBuildConfigValue(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

val algoliaAppId = configValue("ALGOLIA_APP_ID").ifBlank { "6LHV24KBWJ" }
val algoliaSearchApiKey = configValue("ALGOLIA_SEARCH_API_KEY")
    .ifBlank { "34f1596d1dabef7bf8ec268a6219e5d6" }
val algoliaExercisesIndexName = configValue("ALGOLIA_EXERCISES_INDEX_NAME")
    .ifBlank { "fitfusion_exercises_algolia" }

android {
    namespace = "com.example.fitfusion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitfusion"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ALGOLIA_APP_ID",
            "\"${escapedBuildConfigValue(algoliaAppId)}\""
        )
        buildConfigField(
            "String",
            "ALGOLIA_SEARCH_API_KEY",
            "\"${escapedBuildConfigValue(algoliaSearchApiKey)}\""
        )
        buildConfigField(
            "String",
            "ALGOLIA_EXERCISES_INDEX_NAME",
            "\"${escapedBuildConfigValue(algoliaExercisesIndexName)}\""
        )
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //HEALTH CONNECT
    implementation("androidx.health.connect:connect-client:1.1.0")

    //COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    //FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    //COIL
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    implementation("io.coil-kt.coil3:coil-video:3.0.4")

    //CAMERAX
    val cameraxVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

}
