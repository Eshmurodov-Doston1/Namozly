plugins {
    alias(libs.plugins.namozly.android.library.compose)
}

dependencies {
    api(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.coil.compose)
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
}
