package com.psgpu.android.filter

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * Vignette Effect Filter.
 *
 * @param vignetteStart ビネットが始まる中心からの距離 (例: 0.3)
 * @param vignetteEnd ビネットが完全に黒になる距離 (例: 0.75)
 * @param center 中心
 * @param vignetteColor ビネットの色 (alpha is not considered)
 * */
class PSVignetteFilter(
    @FloatRange(from = 0.0) private var vignetteStart: Float = 0.75f,
    @FloatRange(from = 0.0) private var vignetteEnd: Float = 1f,
    private var center: Pair<Float, Float> = Pair(0.5f, 0.5f),
    @ColorInt private var vignetteColor: Int = Color.BLACK
): PSTemplateFilter(
    params = PSTemplateFilterParams(
        fragmentShaderSrcPath = "shader/Vignette.fsh"
    )
) {
    fun setParams(
        vignetteStart: Float? = null,
        vignetteEnd: Float? = null,
        center: Pair<Float?, Float?>? = null,
        @ColorInt vignetteColor: Int? = null
    ) {
        this.vignetteStart = vignetteStart ?: this.vignetteStart
        this.vignetteEnd = vignetteEnd ?: this.vignetteEnd
        this.center = if (center != null) {
            Pair(center.first ?: this.center.first, center.second ?: this.center.second)
        } else {
            this.center
        }
        this.vignetteColor = vignetteColor ?: this.vignetteColor
    }

    override fun setupCustomUniformParams(width: Int, height: Int) {
        val color = Color.valueOf(vignetteColor)
        params.uniformParams = listOf(
            PSUniformParam.F1("u_vignetteStart", vignetteStart),
            PSUniformParam.F1("u_vignetteEnd", vignetteEnd),
            PSUniformParam.F2("u_center", center.first, center.second),
            PSUniformParam.F3("u_vignetteColor", color.red(), color.green(), color.blue())
        )
    }

    fun getVignetteStart() = vignetteStart
    fun getVignetteEnd() = vignetteEnd
    fun getCenter() = center
    fun getVignetteColor() = vignetteColor
}