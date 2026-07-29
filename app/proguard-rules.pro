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

# Models deserialized by Gson. Without this, R8 strips the generic signature of
# collection fields (e.g. QuizFullQuestion.answers: List<QuizAnswer>), so Gson
# resolves the element type as Object and produces LinkedTreeMap instances,
# which then throw ClassCastException at the first field access.
-keep class bassamalim.hidaya.core.models.** { *; }