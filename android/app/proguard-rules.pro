# MediaPipe Tasks GenAI
-keep class com.google.mediapipe.tasks.genai.llminference.** { *; }
-keepclassmembers class com.google.mediapipe.tasks.genai.llminference.** { *; }
-dontwarn com.google.mediapipe.**

# Keep native methods (used by MediaPipe JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Kotlinx Serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
