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
-keep class com.bolyartech.forge.skeleton.dagger.basic.misc.ForgeHeaderResultProducer { *; }
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