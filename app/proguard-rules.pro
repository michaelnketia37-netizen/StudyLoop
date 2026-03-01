# StudyLoop ProGuard rules

# Keep Room entities and DAOs
-keep class com.studyloop.core.model.** { *; }
-keep class com.studyloop.core.database.** { *; }

# Keep Hilt entry points
-keep class dagger.hilt.** { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Keep Firebase models
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Keep AdMob
-keep class com.google.android.gms.ads.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
