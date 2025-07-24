package com.psgpu.android.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * Look up filter.
 *
 * @param intensity 強度 0はオリジナル
 * @param lutBitmap Look Up Table (LUT)
 * @param lutGridSize　LUTの一辺にタイルいくつあるか
 * @param lutTileSize LUTのタイルの一辺にpixelがいくつあるか
 * */
class PSLookUpFilter(
    @FloatRange(from = 0.0) private var intensity: Float = 0.1f,
    private var lutBitmap: Bitmap,
    @IntRange(from = 1) private var lutGridSize: Int,
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