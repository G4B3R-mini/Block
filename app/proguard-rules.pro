####################################################################################################
# Sentry
####################################################################################################

# Recommended config via https://docs.sentry.io/clients/java/modules/android/#manual-integration
# Since we don't obfuscate, we don't need to use their Gradle plugin to upload ProGuard mappings.
-keepattributes LineNumberTable,SourceFile
-dontwarn org.slf4j.**
-dontwarn javax.**

# Our addition: this class is saved to disk via Serializable, which ProGuard doesn't like.
# If we exclude this, upload silently fails (Sentry swallows a NPE so we don't crash).
# I filed https://github.com/getsentry/sentry-java/issues/572
#
# If Sentry ever mysteriously stops working after we upgrade it, this could be why.
-keep class io.sentry.event.Event { *; }

####################################################################################################
# Android and GeckoView built-ins
####################################################################################################

-dontwarn android.**
-dontwarn androidx.**
-dontwarn com.google.**
-dontwarn org.mozilla.geckoview.**

# Raptor now writes a *-config.yaml file to specify Gecko runtime settings (e.g. the profile dir). This
# file gets deserialized into a DebugConfig object, which is why we need to keep this class
# and its members.
-keep class org.mozilla.gecko.util.DebugConfig { *; }

####################################################################################################
# kotlinx.coroutines: use the fast service loader to init MainDispatcherLoader by including a rule
# to rewrite this property to return true:
# https://github.com/Kotlin/kotlinx.coroutines/blob/8c98180f177bbe4b26f1ed9685a9280fea648b9c/kotlinx-coroutines-core/jvm/src/internal/MainDispatchers.kt#L19
#
# R8 is expected to optimize the default implementation to avoid a performance issue but a bug in R8
# as bundled with AGP v7.0.0 causes this optimization to fail so we use the fast service loader instead. See:
# https://github.com/mozilla-mobile/focus-android/issues/5102#issuecomment-897854121
#
# The fast service loader appears to be as performant as the R8 optimization so it's not worth the
# churn to later remove this workaround. If needed, the upstream fix is being handled in
# https://issuetracker.google.com/issues/196302685
####################################################################################################
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatcherLoader {
    boolean FAST_SERVICE_LOADER_ENABLED return true;
}

####################################################################################################
# Remove debug logs from release builds
####################################################################################################
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

####################################################################################################
# Mozilla Application Services
####################################################################################################

-keep class mozilla.appservices.** { *; }

####################################################################################################
# ViewModels
####################################################################################################

-keep class org.mozilla.fenix.**ViewModel { *; }

####################################################################################################
# Adjust
####################################################################################################

-keep public class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
-keep class dalvik.system.VMRuntime {
    java.lang.String getRuntime();
}
-keep class android.os.Build {
    java.lang.String[] SUPPORTED_ABIS;
    java.lang.String CPU_ABI;
}
-keep class android.content.res.Configuration {
    android.os.LocaledList getLocales();
    java.util.Locale locale;
}
-keep class android.os.LocaleList {
    java.util.Locale get(int);
}

# Keep code generated from Glean Metrics
-keep class org.mozilla.fenix.GleanMetrics.** {  *; }

# Keep motionlayout internal methods
# https://github.com/mozilla-mobile/fenix/issues/2094
-keep class androidx.constraintlayout.** { *; }

# Keep adjust relevant classes
-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

# Keep Android Lifecycle methods
# https://bugzilla.mozilla.org/show_bug.cgi?id=1596302
-keep class androidx.lifecycle.** { *; }


####################################################################################################
# Java
####################################################################################################

-dontwarn java.beans.BeanInfo
-dontwarn java.beans.FeatureDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor

# JNA - prevent native library stripping
-keep class com.sun.jna.** { *; }
-dontwarn com.sun.jna.**
-keepattributes *Annotation*,Signature

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

####################################################################################################
# Protobuf
####################################################################################################

# Keep all protobuf classes and their members
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite$* { *; }
-keep class * implements com.google.protobuf.Internal$EnumLite { *; }
-keep class * implements com.google.protobuf.Internal$EnumVerifier { *; }

# Keep protobuf core classes
-keep class com.google.protobuf.** { *; }

# Optimize for Android
-assumenosideeffects class com.google.protobuf.Android {
    static boolean ASSUME_ANDROID return true;
}

# Optional: If you only use specific protobuf classes, replace the wildcards above with specific classes:
# -keep class com.yourpackage.proto.** { *; }

####################################################################################################
# GeckoView Process Services - CRITICAL for runtime service creation
####################################################################################################

# Keep all GeckoView process classes and their inner classes
-keep class org.mozilla.gecko.process.** { *; }
-keepnames class org.mozilla.gecko.process.** { *; }

# Specifically keep service allocator and process manager
-keep class org.mozilla.gecko.process.ServiceAllocator { *; }
-keep class org.mozilla.gecko.process.GeckoProcessManager { *; }
-keep class org.mozilla.gecko.process.GeckoProcessManager$* { *; }

# Keep all service classes that might be dynamically instantiated
-keep class * extends org.mozilla.gecko.process.GeckoChildProcessServices { *; }
-keep class org.mozilla.gecko.process.GeckoChildProcessServices { *; }
-keep class org.mozilla.gecko.process.GeckoChildProcessServices$* { *; }

# Keep service component names from being obfuscated
-keepnames class org.mozilla.gecko.process.GeckoChildProcessServices$*

# Additional GeckoView process rules
-keep class org.mozilla.gecko.mozglue.** { *; }
-keep class org.mozilla.gecko.annotation.** { *; }
-keep class org.mozilla.gecko.util.ThreadUtils { *; }

####################################################################################################
# Service and Component preservation for dynamic loading
####################################################################################################

# Keep all service classes from obfuscation since they're referenced in manifest
-keep public class * extends android.app.Service
-keepnames public class * extends android.app.Service

# Keep component info
-keep class android.content.ComponentName { *; }
-keepattributes *Annotation*

####################################################################################################
# GeckoView
####################################################################################################

-keep class org.mozilla.gecko.** { *; }
-keepnames class org.mozilla.gecko.** { *; }
-keepclassmembers class org.mozilla.gecko.** { *; }
-dontwarn org.mozilla.gecko.**