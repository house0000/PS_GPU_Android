package com.psgpu.android.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/** Look up filter. */
class PSLookUpFilter(
    // Intensity
    @FloatRange(from = 0.0) private var intensity: Float = 0.1f,
    // Look Up Table (LUT)
    private var lutBitmap: Bitmap,
    // tiles per bitmap one line.
    @IntRange(from = 1) private var lutGridSize: Int,
    // pixels per tile one line.
    @IntRange(from = 1) private var lutTileSize: Int
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/LookUp.fsh"
    )
) {
    fun setParams(
        intensity: Float? = null,
        lutBitmap: Bitmap? = null,
        lutGridSize: Int? = null,
        lutTileSize: Int? = null
    ) {
        this.intensity = intensity ?: this.intensity
        this.lutBitmap = lutBitmap ?: this.lutBitmap
        this.lutGridSize = lutGridSize ?: this.lutGridSize
        this.lutTileSize = lutTileSize ?: this.lutTileSize
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_intensity", intensity),
            PSUniformParam.Texture2D(
                "u_lookupTexture",
                lutBitmap,
                wrapS = GLES20.GL_REPEAT,
                wrapT = GLES20.GL_REPEAT
            ),
            PSUniformParam.I1("u_gridSize", lutGridSize),
            PSUniformParam.I1("u_tileSize", lutTileSize)
        )
    }

    fun getIntensity() = intensity
    fun getLutBitmap() = lutBitmap
    fun getLutGridSize() = lutGridSize
    fun getLutTileSize() = lutTileSize
}