import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature modul qoidasi: feature faqat core:domain / core:model / core:ui / core:designsystem
 * ga bog'lanadi. core:data, core:network, core:database ga TO'G'RIDAN-TO'G'RI bog'lanish TAQIQLANGAN —
 * ular faqat :app orqali Hilt bilan ulanadi.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("namozly.android.library.compose")
            pluginManager.apply("namozly.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:ui"))

                add("implementation", libs.lib("androidx-hilt-navigation-compose"))
                add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.lib("androidx-navigation-compose"))
                add("implementation", libs.lib("kotlinx-serialization-json"))
                add("implementation", libs.lib("kotlinx-coroutines-android"))
            }
        }
    }
}
