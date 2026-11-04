plugins {
    alias(libs.plugins.namozly.android.library)
    alias(libs.plugins.namozly.hilt)
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.androidx.datastore.preferences)
}
