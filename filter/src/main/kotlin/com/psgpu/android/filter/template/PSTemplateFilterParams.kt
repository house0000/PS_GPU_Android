package com.psgpu.android.filter.template

import android.graphics.Bitmap
import android.opengl.GLES20
import androidx.annotation.IntRange

// デフォルトはオリジナルのBitmapを再描画するだけ
data class PSTemplateFilterParams(
    /**
     * If custom this, .vsh must has
     *
     * // 頂点座標
     * attribute vec4 a_Position;
     *
     * // テクスチャ座標
     * attribute vec2 a_TexCoord;
     *
     * and don't bind data with them by yourself (bind internal).
     * */
    // If custom this, must has a_Position, a_TexCoord and don't bind data with them by yourself (bind internal).
    val vertexShaderSrcPath: String = "shader/FillTexture.vsh",
    /**
     * If custom this, .fsh must has
     *
     * // テクスチャユニット
     * uniform sampler2D u_Texture;
     *
     * and don't bind data with them by yourself (bind internal).
     * */
    val fragmentShaderSrcPath: String= "shader/FillTexture.fsh",
    val vertexAttributeParams: List<PSVertexAttributeParam> = emptyList(),
    var uniformParams: List<PSUniformParam> = emptyList()
)

sealed class PSVertexAttributeParam {
    data class FloatVector(
        val nameOnShader: String,
        @IntRange(from = 1, to = 4) val vecSize: Int,
        val data: List<Float>
    ): PSVertexAttributeParam()
}
sealed class PSUniformParam {
    data class Texture2D(
        val nameOnShader: String,
        val bitmap: Bitmap,
        val minFilter: Int = GLES20.GL_LINEAR,
        val magFilter: Int = GLES20.GL_LINEAR,
        val wrapS: Int = GLES20.GL_CLAMP_TO_EDGE,
        val wrapT: Int = GLES20.GL_CLAMP_TO_EDGE
    ): PSUniformParam()

    data class F1(
        val nameOnShader: String,
        val value: Float
    ): PSUniformParam()

    data class F2(
        val nameOnShader: String,
        val valueX: Float,
        val valueY: Float
    ): PSUniformParam()

    data class F3(
        val nameOnShader: String,
        val x: Float,
        val y: Float,
        val z: Float
    ): PSUniformParam()

    data class I1(
        val nameOnShader: String,
        val value: Int
    ): PSUniformParam()

    data class I2(
        val nameOnShader: String,
        val valueX: Int,
        val valueY: Int
    ): PSUniformParam()
}