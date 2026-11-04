plugins {
    alias(libs.plugins.namozly.android.library)
    alias(libs.plugins.namozly.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(projects.core.common)
    api(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    api(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    api(libs.kotlinx.serialization.json)
}
