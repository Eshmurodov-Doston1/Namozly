plugins {
    alias(libs.plugins.namozly.android.feature)
}

dependencies {
    // Makka/Madina jonli efiri ilova ichida — HLS oqim + o'z boshqaruv paneli.
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.activity.compose)
}
