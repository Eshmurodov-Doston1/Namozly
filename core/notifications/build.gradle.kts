plugins {
    alias(libs.plugins.namozly.android.library)
    alias(libs.plugins.namozly.hilt)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
