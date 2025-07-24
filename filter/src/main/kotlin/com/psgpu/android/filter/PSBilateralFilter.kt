package com.psgpu.android.filter

import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * バイラテラルフィルター.
 * エッジを保ちながらブラーをかける。
 *
 * @param spatialSigma 空間距離の強さ（ガウス関数の係数）(例: 4.0)
 * @param colorSigma  色の類似度の強さ（ガウス関数の係数）(例: 0.1)
 * @param radius 1pixelを計算するために、半径どれくらいまで計算に使うか。滑らかさなどに影響する。
 * */
class PSBilateralFilter(
    @FloatRange(from = 0.01) private var spatialSigma: Float = 3f,
    @FloatRange(from = 0.01) private var colorSigma: Float = 3f,
    private var radius: Int = 2
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Bilateral.fsh"
    )
) {
    fun setParams(
        spatialSigma: Float? = null,
        colorSigma: Float? = null,
        radius: Int? = null
    ) {
        this.spatialSigma = spatialSigma ?: this.spatialSigma
        this.colorSigma = colorSigma ?: this.colorSigma
        this.radius = radius ?: this.radius
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_spatialSigma", spatialSigma),
            PSUniformParam.F1("u_colorSigma", colorSigma),
            PSUniformParam.I1("u_radius", radius),

            PSUniformParam.I2("u_TextureResolution", width, height)
        )
    }

    fun getSpatialSigma() = spatialSigma
    fun getColorSigma() = colorSigma
    fun getRadius() = radius
}