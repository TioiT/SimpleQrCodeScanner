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

# 保持 activity 不被混淆
-keep public class * extends android.app.Activity
# 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# config 类SDK 类

-keep class com.google.sdk.config.ScanConfig {
            public <methods>;
}
-keep class com.google.sdk.config.DecodeFormat {*;}
-keep class com.google.sdk.config.FlashLightMode {*;}
-keep class com.tckj.config.QrCodeConstant {*;}
-keep class com.google.sdk.ScanCodeSDK {*;}
-keepclasseswithmembers class com.google.sdk.decode.DecodeResult{
    <fields>;
    <methods>;
}
-keep class com.google.sdk.decode.DecodeResultListener {*;}
-keep class com.google.sdk.encode.EncodeResultListener {*;}


#