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
        fragmentShaderSrcPath = "shader/Sharpen.fsh",
        uniformParams = listOf(
            PSUniformParam.F1("u_intensity", intensity),
            PSUniformParam.I1("u_TextureWidth", 0),
            PSUniformParam.I1("u_TextureHeight", 0)
        )
    )
) {
    fun setParams(intensity: Float) {
        this.intensity = intensity

        params.uniformParams = listOf(
            PSUniformParam.F1("u_intensity", intensity)
        )
    }

    fun getIntensity(): Float = intensity

    override fun runGraphicPipeline(inputTexture: PSTextureObject, width: Int, height: Int): PSFBO {
        params.uniformParams = params.uniformParams + PSUniformParam.I1("u_TextureWidth", width)
        params.uniformParams = params.uniformParams + PSUniformParam.I1("u_TextureHeight", height)

        return super.runGraphicPipeline(inputTexture, width, height)
    }
}