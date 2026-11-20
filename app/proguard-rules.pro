# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in default_proguard_files/global/proguard-android-optimize.txt.

# Dotap uses no reflection or network serialization.
# AAPT automatically preserves AndroidManifest entry points.

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Strip debug/diagnostic Android logs from production release builds.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}
