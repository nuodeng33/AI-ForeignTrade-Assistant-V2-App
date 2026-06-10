plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.maolianzhihe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.maolianzhihe"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["usesCleartextTraffic"] = "true"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.3:1337/api/\"")
        }
        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.3:1337/api/\"")
        }
        create("prod") {
            dimension = "environment"
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/api/\"")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX 核心库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // UI 组件
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // 安全存储
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
