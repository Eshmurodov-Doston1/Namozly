plugins {
    alias(libs.plugins.namozly.android.library)
    alias(libs.plugins.namozly.hilt)
    alias(libs.plugins.namozly.android.room)
}

dependencies {
    implementation(projects.core.model)
    api(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.core)
}
