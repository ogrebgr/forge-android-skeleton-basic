# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /projects_src/android_sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn javax.mail.**

-keep class ch.qos.** { *; }
-keep class com.bolyartech.forge.exchange.ForgeExchangeResult { *; }
-keep class com.bolyartech.forge.base.exchange.forge.ForgeHeaderResultProducer { *; }
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}
-keep class * implements android.os.Parcelable { *; }
-keepattributes SourceFile,LineNumberTable
-keep class org.acra.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.*

-keep class com.google.j2objc.annotations.** { *; }
-dontwarn   com.google.j2objc.annotations.**
-keep class java.lang.ClassValue { *; }
-dontwarn   java.lang.ClassValue
-keep class org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement { *; }
-dontwarn   org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

## Retrolambda specific rules ##
# as per official recommendation: https://github.com/evant/gradle-retrolambda#proguard
-dontwarn java.lang.invoke.*

# streamsupport
-keep class java8.**
-dontwarn java8.**

-dontwarn org.checkerframework.checker.**
-dontwarn afu.org.checkerframework.checker.**
-dontwarn com.google.errorprone.annotations.**
