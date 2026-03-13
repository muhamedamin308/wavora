plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.wavora.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wavora.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            // Explicitly disable shrinking for debug to ensure fast builds
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
        }
        release {
            // Option 1: To just get it working for Phase 1
            isMinifyEnabled = false
            isShrinkResources = false

            // Option 2: If you want to prepare for Phase 8 (Production-Ready)
            // isMinifyEnabled = true
            // isShrinkResources = true

            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Export Room migration schemas alongside source
    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // ── AndroidX Core ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splash.screen)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.palette.ktx)

    // ── Compose BOM (pins ALL compose versions together) ─────────────────────
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)

    // ── Hilt Dependency Injection ─────────────────────────────────────────────
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Media3 / ExoPlayer ────────────────────────────────────────────────────
    implementation(libs.bundles.media3)
    implementation(libs.androidx.media)

    // ── Room Database ─────────────────────────────────────────────────────────
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ── DataStore ─────────────────────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── Coil Image Loading ────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── WorkManager ──────────────────────────────────────────────────────────
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler) // Use ksp as per your setup
    implementation(libs.androidx.startup)

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ── Unit Tests ────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.arch.testing)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    // ── Instrumented Tests ────────────────────────────────────────────────────
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

// Room schema export directory for migration tracking
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}