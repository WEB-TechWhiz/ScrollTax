# ProGuard rules for billing module
-keepattributes *Annotation*
-keepclassmembers class * {
    @javax.inject.* <methods>;
    @dagger.* <methods>;
    @com.google.dagger.* <methods>;
}
