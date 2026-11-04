# kotlinx.serialization (Retrofit DTO va Navigation route'lari)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer { *; }

# Retrofit
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Yandex MapKit
-keep class com.yandex.** { *; }
-dontwarn com.yandex.**

# WebView JS bridge (jonli efir)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
