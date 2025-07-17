package com.psgpu.filter.filter

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

interface PSFilter {

    /** Create input texture. */

    /** Run graphic pipeline. */

    /** Create output bitmap. */

    /** Create filtered bitmap from input bitmap. */
    fun apply(inputBitmap: Bitmap): Bitmap {
        /** Create input texture */

        /** Run graphic pipeline */

        /** Create output bitmap */

        return createBitmap(0, 0)
    }
}