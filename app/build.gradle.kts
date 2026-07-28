plugins {
    id(\"com.android.application\")
    id(\"org.jetbrains.kotlin.android\")
}
android {
    namespace = \"com.petoverflow.app\"
    compileSdk = 34
    defaultConfig {
        applicationId = \"com.petoverflow.app\"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = \"1.0\"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = \"17\" }
}
dependencies {
    implementation(\"androidx.kore:core-ktx:1.12.0\")
    implementation(\"androidx.xappcompat:appcompat:1.6.1\")
    implementation(\"com.squareup.okhttp3:okhttp:4.12.0\")
    implementation(\"org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3\")
    implementation(\"com.google.code.gson:gson:2.10.1\")
}