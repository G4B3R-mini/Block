plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
extra.apply {
    // geckoview version
    set("geckoviewChannel", "Stable")
    set("geckoviewVersion", "100.0.20220425210429")
}
android {
    namespace = "com.shmibblez.inferno"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shmibblez.inferno"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        // geckoview uses java 17 apis
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // geckoview implementation
    implementation("org.mozilla.geckoview:geckoview-${project.extra["geckoviewChannel"]}:${project.extra["geckoviewVersion"]}")
    // mozilla android components
    implementation(libs.browser.state)
    implementation(libs.browser.toolbar)
    implementation(libs.browser.domains)
    implementation(libs.browser.errorpages)
    implementation(libs.browser.search)
    implementation(libs.browser.tabstray)
    implementation(libs.browser.menu)

    // default deps
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}