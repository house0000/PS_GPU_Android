package com.psgpu.android.filter

import android.graphics.Bitmap
import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam
import com.psgpu.android.gl.model.PSTextureObject

/**
 * Gaussian Blur Filter
 *
 * @param radius 計算半径。でかいと滑らかになるが計算量が増える。
 * @param sigma ガウス関数の係数。でかいと周りのピクセルの影響がデカくなる、のでよりのっぺりする。
 *
 * */
data class PSGaussianBlurFilter(
    private var radius: Int = 10,
    @FloatRange(from = 0.01) private var sigma: Float = 10f
): PSFilter {
    private val horizontalBlurFilter = PSGaussianBlurHorizontalFilter(radius, sigma)
    private val verticalBlurFilter = PSGaussianBlurVerticalFilter(radius, sigma)
    private val groupFilter = PSGroupFilter(horizontalBlurFilter, verticalBlurFilter)

    fun setParams(
        radius: Int? = null,
        sigma: Float? = null
    ) {
        this.radius = radius ?: this.radius
        this.sigma = sigma ?: this.sigma
        horizontalBlurFilter.setParams(this.radius, this.sigma)
        verticalBlurFilter.setParams(this.radius, this.sigma)
    }

    fun getRadius() = radius
    fun getSigma() = sigma

    override fun runGraphicPipeline(inputTexture: PSTextureObject, width: Int, height: Int) = groupFilter.runGraphicPipeline(inputTexture, width, height)
    override fun releaseGLObjects() = groupFilter.releaseGLObjects()
    override fun apply(inputBitmap: Bitmap) = groupFilter.apply(inputBitmap)
}

private data class PSGaussianBlurHorizontalFilter(
    private var radius: Int,
    @FloatRange(from = 0.01) private var sigma: Float
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/GaussianBlurHorizontal.fsh",
    )
) {
    fun setParams(
        radius: Int? = null,
        sigma: Float? = null
    ) {
        this.radius = radius ?: this.radius
        this.sigma = sigma ?: this.sigma
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.I1("u_kernel", this.radius),
            PSUniformParam.F1("u_sigma", this.sigma),
            PSUniformParam.I1("u_TextureWidth", width)
        )
    }
}

private data class PSGaussianBlurVerticalFilter(
    private var radius: Int,
    @FloatRange(from = 0.01) private var sigma: Float
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/GaussianBlurVertical.fsh"
    )
) {
    fun setParams(
        radius: Int? = null,
        sigma: Float? = null
    ) {
        this.radius = radius ?: this.radius
        this.sigma = sigma ?: this.sigma
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.I1("u_kernel", this.radius),
            PSUniformParam.F1("u_sigma", this.sigma),
            PSUniformParam.I1("u_TextureHeight", height)
        )
    }
}