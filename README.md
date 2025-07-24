# PSGPU-Android


## Install

```kotlin :settings.gradle.kts
// settings.gradle.kts

dependencyResolutionManagement {
    repositories {
        // Add
        maven { url 'https://jitpack.io' }
    }
}
```

```kotlin :module/build.gradle.kts
// module/build.gradle.kts

dependencies {
    // Add
    implementation("com.github.house0000:PS_GPU_Android:1.0.0")
}
```

## How to use

### Filter

```kotlin
// Setup
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PSFilter.init(this)
    }
}
```


```kotlin
// init
val filter = PSGaussianFilter(
    radius = 5,
    sigma = 30
)

// change params if needed
filter.setParams(radius = 10)

// apply filter to Bitmap
val filteredBitmap = filter.apply(bitmap)

```

## Samples
Run this project.