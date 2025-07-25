package com.psgpu.android.filter

import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * 彩度を調整するフィルター
 *
 * @param saturation: 0がグレースケール、1がオリジナル、それ以上だと彩度が強くなる。
 *
 * */
class PSSaturationFilter(
    private var saturation: Float = 1f
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Saturation.fsh"
    )
) {
    fun setParams(@FloatRange(from = 0.0) saturation: Float) {
        this.saturation = saturation
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        params.uniformParams = listOf(
            PSUniformParam.F1("u_saturation", saturation)
        )
    }

    fun getSaturation(): Float = saturation
}