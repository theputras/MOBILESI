plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.newproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.newproject"
        minSdk = 34
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Tambahkan baris-baris ini
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
}

