import java.util.Properties

plugins {
    alias(libs.plugins.namozly.android.application)
    alias(libs.plugins.namozly.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Maxfiy kalitlar local.properties'da saqlanadi (git'ga tushmaydi):
// YANDEX_MAPKIT_API_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "uz.dostonbek.namozly"

    defaultConfig {
        applicationId = "uz.dostonbek.namozly"
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "YANDEX_MAPKIT_API_KEY", "\"${localProps.getProperty("YANDEX_MAPKIT_API_KEY", "1679252a-bd27-4314-9146-6634f6461607")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.media)
    implementation(projects.core.notifications)

    implementation(projects.feature.home)
    implementation(projects.feature.books)
    implementation(projects.feature.quran)
    implementation(projects.feature.qibla)
    implementation(projects.feature.tasbih)
    implementation(projects.feature.calendar)
    implementation(projects.feature.mosques)
    implementation(projects.feature.names)
    implementation(projects.feature.duas)
    implementation(projects.feature.qaza)
    implementation(projects.feature.ramadan)
    implementation(projects.feature.learn)
    implementation(projects.feature.live)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material.icons.extended)
}
