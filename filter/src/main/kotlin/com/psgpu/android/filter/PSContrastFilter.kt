package com.psgpu.android.filter

import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * 明度差を調整するフィルター
 *
 * @param contrast 0だとグレー一色、1がオリジナル、それ以上は明度差が強化される。
 * */
class PSContrastFilter(
    @FloatRange(from = 0.0) private var contrast: Float = 1f
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Contrast.fsh"
    )
) {
    fun setParams(@FloatRange(from = 0.0) contrast: Float) {
        this.contrast = contrast
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_contrast", contrast)
        )
    }

    fun getContrast(): Float = contrast
}