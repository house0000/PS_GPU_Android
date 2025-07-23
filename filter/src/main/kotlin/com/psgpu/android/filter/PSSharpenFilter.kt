package com.psgpu.android.filter

import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSTextureObject

class PSSharpenFilter(
    private var intensity: Float = 0f
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Sharpen.fsh"
    )
) {
    fun setParams(intensity: Float) {
        this.intensity = intensity
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_intensity", intensity),
            PSUniformParam.I1("u_TextureWidth", width),
            PSUniformParam.I1("u_TextureHeight", height)
        )
    }

    fun getIntensity(): Float = intensity
}