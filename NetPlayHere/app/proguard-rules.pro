# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/lining/Library/Android/sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5 # 指定代码的压缩级别
-dontusemixedcaseclassnames # 是否使用大小写混合
-dontpreverify # 混淆时是否做预校验
-verbose # 混淆时是否记录日志

-optimizations !code/simplification/arithmetic,!field/,!class/merging/ # 混淆时所采用的算法

-keep public class * extends android.app.Application # 保持哪些类不被混淆
-keep public class * extends android.app.Service # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService # 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.v4.**

-keep class * extends android.app.Activity{*;}
-keep class * extends android.app.Service{*;}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application {*;}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {*;}

-keepclasseswithmembernames class * { # 保持 native 方法不被混淆
native <methods>;
}

-keepclasseswithmembers class * { # 保持自定义控件类不被混淆
public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
public void *(android.view.View);
}

-keepclassmembers enum * { # 保持枚举 enum 类不被混淆
public static **[] values();
public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembers class * {
public <init>(android.content.Context);
}

-keep public class org.** {*;}

-dontwarn org.mockito.**
-dontwarn sun.reflect.**
-dontwarn android.test.**
-dontwarn android.support.v4.**
-dontwarn com.umeng.**
-dontwarn alipaySdk-20160111.**
-dontwarn BaiduLBS_Android.**
-dontwarn barcode_core.**
-dontwarn com.umeng.message.lib_v2.6.0.**
-dontwarn libammsdk.**
-dontwarn picasso-2.4.0.**
-dontwarn umeng-analytics-v5.6.4.**
-dontwarn umeng-update-v2.6.0.1.**
-dontwarn UPPayAssistEx.**
-dontwarn UPPayPluginExPro.**
-dontwarn xUtils-2.6.14.**

-dontwarn commons-codec-1.6.**
-dontwarn commons-logging-1.1.1.**
-dontwarn fluent-hc-4.2.5.**
-dontwarn httpclient-4.2.5.**
-dontwarn httpclient-cache-4.2.5.**
-dontwarn httpcore-4.2.4.**
-dontwarn httpmime-4.2.5.**

-dontwarn com.google.zxing.**
-dontwarn java.awt.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.http.**

-keep class com.lidroid.xutils.**{*;}
-keepattributes *Annotation
-keep class * extends java.lang.annotation.Annotation { *; }

-keep class com.baidu.** { *; }
-keep class com.umeng.{*;}
-keep class com.unionpay.{*;}
-keep class com.alipay.**{*;}

-keep class android.annotation.SuppressLint.**{*;}
-keep class android.content.Context.**{*;}
-keep class com.lidroid.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }

-keep,allowshrinking class org.android.agoo.service.* {
public <fields>;
public <methods>;
}
-keep,allowshrinking class com.umeng.message.* {
public <fields>;
public <methods>;
}
-keep public class com.kaifeng.trainee.app.R$*{
public static final int *;
}

-keepclassmembers class * implements java.io.Serializable {
*;
}
-keep class com.google.zxing.** { *; }
-keep class java.awt.** { *; }
-keep class org.apache.commons.logging.** { *; }
-keep class org.apache.http.** { *; }


-dontskipnonpubliclibraryclassmembers
-dontwarn com.baidu.**
-dontwarn cn.bmomb.**