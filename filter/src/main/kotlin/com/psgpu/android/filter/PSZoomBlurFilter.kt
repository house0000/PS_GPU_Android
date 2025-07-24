package com.psgpu.android.filter

import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.filter.template.PSTemplateFilterParams
import com.psgpu.android.filter.template.PSUniformParam

/**
 * ズームブラー効果を適用するフィルターです。
 *
 * 指定された中心点から放射状に画像をぼかし、カメラのズーム中に発生するような
 * モーションブラーを表現します。
 *
 * @param intensity ブラーの強度。値が大きいほど、放射状に伸びるブラーが長くなります。
 * @param blurCenter ブラーの中心点。画像の正規化座標で指定します（(0.0, 0.0)が左下、(1.0, 1.0)が右上）。デフォルトの `(0.5f, 0.5f)` は画像の中心を意味します。
 * @param samples ブラー計算時のサンプリング回数。値を大きくするとブラーの品質は向上しますが、その分、計算負荷が高くなります。
 */
class PSZoomBlurFilter(
    private var intensity: Float = 0.1f,
    private var blurCenter: Pair<Float, Float> = Pair(0.5f, 0.5f),
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