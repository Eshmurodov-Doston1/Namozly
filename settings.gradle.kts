pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Namozly"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// ── Core: sof Kotlin (Android'ga bog'liq emas) ─────────────────────
include(":core:model")
include(":core:common")
include(":core:domain")

// ── Core: Android infratuzilma ─────────────────────────────────────
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:media")
include(":core:notifications")
include(":core:designsystem")
include(":core:ui")

// ── Feature modullar ───────────────────────────────────────────────
include(":feature:home")
include(":feature:books")
include(":feature:quran")
include(":feature:qibla")
include(":feature:tasbih")
include(":feature:calendar")
include(":feature:mosques")
include(":feature:names")
include(":feature:duas")
include(":feature:qaza")
include(":feature:ramadan")
include(":feature:learn")
include(":feature:live")
include(":feature:settings")
