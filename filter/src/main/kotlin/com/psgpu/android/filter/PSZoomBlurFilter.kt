package com.psgpu.android.filter

import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

class PSZoomBlurFilter(
    private var intensity: Float = 0.1f,
    // (0.5f, 0.5f) means center.
    private var blurCenter: Pair<Float, Float> = Pair(0.5f, 0.5f),
    // sampling count. (Quality - Calculation trade-off)
    private var samples: Int = 5
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/ZoomBlur.fsh"
    )
) {
    fun setParams(
        intensity: Float? = null,
        blurCenter: Pair<Float?, Float?>? = null,
        samples: Int? = null
    ) {
        this.intensity = intensity ?: this.intensity
        this.blurCenter = if (blurCenter != null) {
            Pair(blurCenter.first ?: this.blurCenter.first, blurCenter.second ?: this.blurCenter.second)
        } else {
            this.blurCenter
        }
        this.samples = samples ?: this.samples
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_intensity", intensity),
            PSUniformParam.F2("u_blurCenter", blurCenter.first, blurCenter.second),
            PSUniformParam.I1("u_samples", samples),

            PSUniformParam.I2("u_TextureResolution", width, height)
        )
    }

    fun getIntensity() = intensity
    fun getBlurCenter() = blurCenter
    fun getSamples() = samples
}