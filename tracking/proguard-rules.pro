# ProGuard rules for tracking module
-keepattributes *Annotation*
-keepclassmembers class * {
    @javax.inject.* <methods>;
    @dagger.* <methods>;
    @com.google.dagger.* <methods>;
}
