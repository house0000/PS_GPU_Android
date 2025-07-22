package com.psgpu.android.filter

enum class PSFilterType {
    NO_FILTER,
    GAUSSIAN_BLUR,
    SHARPEN;

    val title: String
        get() {
            return when (this) {
                NO_FILTER -> "NO_FILTER"
                GAUSSIAN_BLUR -> "GAUSSIAN_BLUR"
                SHARPEN -> "SHARPEN"
            }
        }
}