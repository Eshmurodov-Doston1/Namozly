package uz.dostonbek.namozly.core.common

import javax.inject.Qualifier

enum class NamozlyDispatchers { Default, IO }

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: NamozlyDispatchers)

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
