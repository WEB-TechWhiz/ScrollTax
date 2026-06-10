# ProGuard rules for Scroll Tax
-keepattributes *Annotation*
-keepclassmembers class * {
    @javax.inject.* <methods>;
    @dagger.* <methods>;
    @com.google.dagger.* <methods>;
}

# Keep Room entities
-keep class com.scrolltax.data.model.** { *; }
-keep class com.scrolltax.data.db.** { *; }

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }

# Keep Lottie
-keep class com.airbnb.lottie.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# General Android
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
