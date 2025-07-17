package com.psgpu.filter.filter

enum class PSFilterType {
    GAUSSIAN_BLUR;

    val title: String
        get() {
            return when (this) {
                GAUSSIAN_BLUR -> "GAUSSIAN_BLUR"
            }
        }
}