package com.psgpu.android.filter

import android.graphics.Bitmap
import com.psgpu.android.gl.GLES20GetTextureAttachedToFBO
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSGLObjects
import com.psgpu.android.gl.model.PSTextureObject

class PSGroupFilter(
    val filters: List<PSFilter>
): PSFilter {
    private var glObjects: PSGLObjects = PSGLObjects()

    constructor(vararg filterArgs: PSFilter) : this(filterArgs.toList())
    override fun releaseGLObjects() {
        // 多分大丈夫なんだけど一応逆から削除していく
        filters.reversed().forEach { it.releaseGLObjects() }
        glObjects.release()
    }

    override fun runGraphicPipeline(inputTexture: PSTextureObject, width: Int, height: Int): PSFBO {
        if (filters.isEmpty()) {
            return PSNoFilter().runGraphicPipeline(inputTexture, width, height)
        } else {
            var preTex = inputTexture

            var outputFbo: PSFBO? = null
            filters.forEachIndexed { index, filter ->
                outputFbo = filter.runGraphicPipeline(preTex, width, height)
                val outputTex = GLES20GetTextureAttachedToFBO(outputFbo.handler)
                preTex = PSTextureObject(outputTex)
            }

            return outputFbo!!
        }
    }

    override fun apply(inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height

        // バリデーションチェック
        // bitmapは有効か
        if (width == 0 || height == 0 || inputBitmap.config == null) {
            throw PSFilterException.InvalideInputBitmap
        }

        // glリリース用コンテナ初期化
        glObjects = PSGLObjects()

        // レンダリング
        enableDrawContext()

        val inputTexture = createInputTexture(inputBitmap)
        glObjects.addTexture(inputTexture.handler)// glオブジェクトは生成したところでリリースの責任を持つ
        val outputFBO = runGraphicPipeline(inputTexture, width, height)
        val outputBitmap = createOutputBitmap(outputFBO, width, height)

        releaseGLObjects()
        releaseDrawContext()

        return outputBitmap
    }
}