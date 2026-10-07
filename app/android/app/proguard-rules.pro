# MMMUT Android App ProGuard & R8 Optimization Rules

# Keep data models used by Gson and Firestore deserialization
-keepclassmembers class com.mmmut.ero.data.model.** { *; }
-keepclassmembers class com.mmmut.ero.data.remote.** { *; }
-keep class com.mmmut.ero.data.model.** { *; }
-keep class com.mmmut.ero.data.remote.** { *; }

# Keep ViewModels and their constructors
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Retrofit / OkHttp reflection & annotations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.firebase.**

# AndroidX Compose rules
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
