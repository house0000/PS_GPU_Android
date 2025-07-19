package com.psgpu.android.filter

import android.graphics.Bitmap
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSTextureObject

data class PSGaussianBlurFilter(
    val blurSize: Float = 1f
): PSFilter {
    override fun runGraphicPipeline(
        inputTexture: PSTextureObject,
        width: Int,
        height: Int
    ): PSFBO {
        TODO("Not yet implemented")
    }

    override fun releaseGLObjects() {
        TODO("Not yet implemented")
    }

    override fun apply(inputBitmap: Bitmap): Bitmap {
        TODO("Not yet implemented")
    }

}
