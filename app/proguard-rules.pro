# ── Retrofit ────────────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.adapter.rxjava3.**

# ── OkHttp ───────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ── Moshi ────────────────────────────────────────────────────────────────────
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier @interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-dontwarn com.squareup.moshi.**

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keepclasseswithmembers class * { @dagger.hilt.* <fields>; }
-dontwarn dagger.hilt.**

# ── Room — keep entities ──────────────────────────────────────────────────────
-keep class com.gamelaunch.data.local.entity.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { abstract *; }

# ── Domain models ─────────────────────────────────────────────────────────────
-keep class com.gamelaunch.domain.model.** { *; }
-keep class com.gamelaunch.data.remote.dto.** { *; }

# ── Sentry — keep stack trace info ───────────────────────────────────────────
-keepattributes LineNumberTable,SourceFile
-dontwarn io.sentry.**
-keep class io.sentry.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepclassmembers class kotlin.Metadata { *; }
