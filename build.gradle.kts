// Top-level build file. Barcha plaginlar shu yerda "apply false" bilan e'lon qilinadi,
// modullar esa build-logic'dagi convention plugin'lar orqali ularni qo'llaydi.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
