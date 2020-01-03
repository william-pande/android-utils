[![](https://jitpack.io/v/WilliamPande/android-utils.svg)](https://jitpack.io/#WilliamPande/android-utils)

[![Maven Central](https://img.shields.io/maven-central/v/com.code-troopers.betterpickers/library.svg?style=flat)](https://repo1.maven.org/maven2/com/code-troopers/betterpickers/library/)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Betterpickers-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/118)

Including in Your Project
=========================
### Gradle

```groovy
    implementation 'com.github.william-pande:android-utils:x.x.x'
```

### You can override the constants
=================================
### The TAG for log cats
```kotlin
LibUtils.TAG = "LIB UTILS"
```

### Display or not display a log
```kotlin
LibUtils.SHOW_LOG = true
```

### Connection connect timeout
```kotlin
LibUtils.CONNECT_TIMEOUT = 10
```

### Connection read time out
```kotlin
LibUtils.READ_TIMEOUT = 10
```


# Connection write time out
```kotlin
LibUtils.WRITE_TIMEOUT = 10
```

# The url for   
```kotlin  
LibUtils.URL_LINK = ""
```

 # User alert dialog colors
 ```xml
<resources>
    <color name="alert_dialog_background">@color/colorPrimary</color>
    <color name="alert_dialog_border">@color/colorPrimaryDark</color>
    <color name="alert_dialog_text_color">@color/white</color>
    <color name="color_progress_cream">#D3CBCB</color>
   
    <color name="alert_positive_color">@color/colorAccent</color>
    <color name="alert_negative_color">@color/colorAccent</color>
    <color name="alert_neutral_cream">@color/colorAccent</color>
</resources>
 ```
 
 # User alert dimensions
 ```xml
 <resources>
    <dimen name="alert_dialog_radius">5dp</dimen>
    <dimen name="alert_dialog_stroke_width">5dp</dimen>
 </resources>
 ```
 
 Contribution
 ===================
 ### Pull requests are welcome!
 Feel free to contribute to BetterPickers.
 If you've fixed a bug or have a feature you've added, just create a pull request.

 If you've found a bug, want a new feature, or have other questions, [file an issue][10]. We will try to answer as soon as possible.