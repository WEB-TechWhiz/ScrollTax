# ProGuard rules for onboarding module
-keepattributes *Annotation*
-keepclassmembers class * {
    @javax.inject.* <methods>;
    @dagger.* <methods>;
    @com.google.dagger.* <methods>;
}
