# PSGPU-Android

![Image](https://github.com/user-attachments/assets/42bafe6a-be0d-4b12-b48d-737e236873a3)

# Install

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
    implementation("com.github.house0000:PS_GPU_Android:version")
}
```

# How to use

## Filter

### use filter
```kotlin
// Setup
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PSFilter.init(this) // Add
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

### custom filter
```kotlin
/**
 * My custom filter
 *
 * @param myParam
 *
 * */
class PSCustomFilter(
    private var myParam: Float = 1f
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        vertexShaderSrcPath = "shader/Custom.vsh",
        fragmentShaderSrcPath = "shader/Custom.fsh"
    )
) {
    fun setParams(myParam: Float) {
        this.myParam = myParam
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_myParam", saturation)
        )
    }

    fun getMyParam(): Float = myParam
}
```

```
assets/
└── shader/
    └── Custom.fsh
    └── Custom.vsh
```



## Samples
Run this project.
