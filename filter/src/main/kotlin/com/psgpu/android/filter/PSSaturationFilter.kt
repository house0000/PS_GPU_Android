package com.psgpu.android.filter

import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

class PSSaturationFilter(
    private var saturation: Float = 1f
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Saturation.fsh",
        uniformParams = listOf(
            PSUniformParam.F1("u_saturation", saturation)
        )
    )
) {
    fun setParams(@FloatRange(from = 0.0) saturation: Float) {
        this.saturation = saturation

        params.uniformParams = listOf(
            PSUniformParam.F1("u_saturation", saturation)
        )
    }

    fun getSaturation(): Float = saturation
}