package com.psgpu.android.filter

/** 使えるフィルター一覧 */
enum class PSFilterType {
    NO_FILTER,
    GAUSSIAN_BLUR,
    SHARPEN,
    SATURATION,
    CONTRAST,
    ZOOM_BLUR,
    VIGNETTE,
    BILATERAL,
    LOOK_UP;

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
                LOOK_UP -> "LOOK_UP"
            }
        }
}