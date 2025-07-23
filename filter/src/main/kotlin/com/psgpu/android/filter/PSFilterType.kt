package com.psgpu.android.filter

enum class PSFilterType {
    NO_FILTER,
    GAUSSIAN_BLUR,
    SHARPEN,
    SATURATION,
    CONTRAST,
    ZOOM_BLUR,
    VIGNETTE,
    BILATERAL;

    val title: String
        get() {
            return when (this) {
                NO_FILTER -> "NO_FILTER"
                GAUSSIAN_BLUR -> "GAUSSIAN_BLUR"
                SHARPEN -> "SHARPEN"
                SATURATION -> "SATURATION"
                CONTRAST -> "CONTRAST"
                ZOOM_BLUR -> "ZOOM_BLUR"
                VIGNETTE -> "VIGNETTE"
                BILATERAL -> "BILATERAL"
            }
        }
}