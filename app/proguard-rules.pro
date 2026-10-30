# Project Specific ProGuard / R8 Rules for Swarix Music Player

# 1. AndroidX Media3 / ExoPlayer ProGuard Rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep class androidx.media3.decoder.** { *; }

# Preserve ExoPlayer extension renderers
-keep class androidx.media3.decoder.vp9.** { *; }
-keep class androidx.media3.decoder.opus.** { *; }
-keep class androidx.media3.decoder.flac.** { *; }
-keep class androidx.media3.decoder.ffmpeg.** { *; }

# 2. Room Database ProGuard Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * {
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}
-keep class com.example.core.db.entity.** { *; }

# 3. Kotlinx Serialization ProGuard Rules
-keepattributes *Annotation*,ElementValueAttribute,InnerClasses,Signature,SuppressedExceptions
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

# 4. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Preserve line numbers for readable crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
