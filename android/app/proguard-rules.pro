# FactLens ProGuard Rules
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.factlens.model.** { *; }