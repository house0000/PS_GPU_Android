package com.psgpu.android.filter

enum class PSFilterType {
    NO_FILTER,
    GAUSSIAN_BLUR,
    SHARPEN,
    SATURATION,
    CONTRAST;

    val title: String
        get() {
            return when (this) {
                NO_FILTER -> "NO_FILTER"
                GAUSSIAN_BLUR -> "GAUSSIAN_BLUR"
                SHARPEN -> "SHARPEN"
                SATURATION -> "SATURATION"
                CONTRAST -> "CONTRAST"
            }
        }
}