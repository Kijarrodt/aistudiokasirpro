# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================
# Kasir Pro - Custom R8/ProGuard Keep Rules
# Ditambahkan untuk mengaktifkan code shrinking
# tanpa merusak fitur yang bergantung reflection
# ============================================

# --- Kotlin and Coroutines ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Firebase Auth and Firestore ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Google Sign-In and Credential Manager ---
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.gms.auth.api.signin.** { *; }
-dontwarn com.google.android.gms.auth.api.signin.**
-dontwarn com.google.android.gms.**

# --- Google Play Billing ---
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# --- App data models and entities ---
-keep class com.kasirpro.app.data.local.** { *; }
-keep class com.kasirpro.app.data.repository.** { *; }
-keep class com.kasirpro.app.data.billing.** { *; }
-keep class com.kasirpro.app.ui.viewmodel.** { *; }

# --- Moshi JSON ---
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-dontwarn com.squareup.moshi.**

# --- Retrofit and OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- Coil Image Loading ---
-dontwarn coil.**

# --- Keep Enums ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
