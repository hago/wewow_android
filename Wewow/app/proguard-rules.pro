# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\v-junsli\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

-ignorewarning 

-keepattributes *Annotation* 
-keepattributes Exceptions 
-keepattributes InnerClasses 
-keepattributes Signature 

# hmscore-support: remote transport 
-keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; } 

# hmscore-support: remote transport 
-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {
	<init>(...); 
} 

# manifest: provider for updates 
-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }


-keep class com.growingio.android.sdk.** {
    *;
}
-dontwarn com.growingio.android.sdk.**
-keepnames class * extends android.view.View
-keep class * extends android.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onHiddenChanged(boolean);
    public void onResume();
    public void onPause();
}
-keep class android.support.v4.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onHiddenChanged(boolean);
    public void onResume();
    public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onHiddenChanged(boolean);
    public void onResume();
    public void onPause();
}
