package com.psgpu.android.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import com.psgpu.android.gl.GLES20CreateEmptyTextureObject
import com.psgpu.android.gl.GLES20CreateFBOColorAttachedTexture2D
import com.psgpu.android.gl.GLES20CreateIBO
import com.psgpu.android.gl.GLES20CreateLinkedProgram
import com.psgpu.android.gl.GLES20CreateVBO
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSGLObjects
import com.psgpu.android.gl.model.PSTextureObject
import com.psgpu.android.gl.readShaderFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/** Just re-generate original bitmap.
 * This filter can be used as base filter implementation.
 */
open class PSNoFilter: PSFilter {
    // シェーダーソース
    private val vertexShaderSrc = readShaderFile(PSFilter.getContext(), "shader/FillTexture.vsh")
    private val fragmentShaderSrc = readShaderFile(PSFilter.getContext(), "shader/FillTexture.fsh")


    // 頂点属性
    private val vertexCoords = floatArrayOf(
        -1.0f,  1.0f, // top left
        1.0f,  1.0f,  // top right
        -1.0f, -1.0f, // bottom left
        1.0f, -1.0f, // bottom right
    )
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f, // top left
        1.0f, 1.0f, // top right
        0.0f, 0.0f, // bottom left
        1.0f, 0.0f  // bottom right
    )

    // 頂点の順番
    private val index = shortArrayOf(
        0, 1, 2,
        3, 2, 1
    )

    private val vertexCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()
        .apply {
            put(vertexCoords).position(0)
        }
    private val textureCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()
        .apply {
            put(textureCoords).position(0)
        }
    private var indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(index.size * 2) // shortは2バイト
    .order(ByteOrder.nativeOrder()).asShortBuffer()
        .apply {
            put(index).position(0)
        }

    private var glObjects: PSGLObjects = PSGLObjects()

    // region GraphicPipeline

    override fun runGraphicPipeline(inputTexture: PSTextureObject, width: Int, height: Int): PSFBO {
        // --------- 出力の準備 ---------

        // 出力テクスチャを生成
        val outputTexture =
            GLES20CreateEmptyTextureObject(width, height)
        glObjects.addTexture(outputTexture)

        // 出力テクスチャのFBOを作っておく
        val fbo = GLES20CreateFBOColorAttachedTexture2D(outputTexture)
        glObjects.addFBO(fbo)

        // --------- グラフィックパイプラインの準備 ---------

        // シェーダプログラムの作成
        glObjects.addProgram(GLES20CreateLinkedProgram(vertexShaderSrc, fragmentShaderSrc))
        val program = glObjects.program!!.program!!
        GLES20.glUseProgram(program)

        // シェーダに頂点属性を渡す(VBO)
        graphicPipelineBindVertexAttributes(program)

        // 頂点のindexを渡す(IBO)
        val ibo = GLES20CreateIBO(indexBuffer, Short.SIZE_BYTES * index.size)
        glObjects.addIBO(ibo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        // --------- グラフィックパイプラインの実行 ---------

        // Uniformを設定する
        graphicPipelineBindUniform(program, inputTexture.handler)

        // 出力FBOのバインド
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo)

        // バインドしたFBOは完全な状態か？
        val boundFBOStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (boundFBOStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw PSFilterException.InvalidFBOStatus
        }

        // キャンバス(FBO)の初期化
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f) // 背景を不透明な黒に
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Draw
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            index.size,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        // fboのアンバインド
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        // outputテクスチャがバインドしたFBOを渡す
        return PSFBO(fbo)
    }

    // overrideできると便利そうなので切り出し
    // 頂点属性を頂点シェーダにバインド
    protected fun graphicPipelineBindVertexAttributes(program: Int) {
        // シェーダに頂点属性を渡す(VBO)
        val vertexCoordsVBO =
            GLES20CreateVBO(vertexCoordsBuffer, Float.SIZE_BYTES * vertexCoords.size)
        glObjects.addVBO(vertexCoordsVBO)
        val positionLocation = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexCoordsVBO)
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, 0)

        val textureCoordsVBO =
            GLES20CreateVBO(textureCoordsBuffer, Float.SIZE_BYTES * textureCoords.size)
        glObjects.addVBO(textureCoordsVBO)
        val texCoordLocation = GLES20.glGetAttribLocation(program, "a_TexCoord")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordsVBO)
        GLES20.glEnableVertexAttribArray(texCoordLocation)
        GLES20.glVertexAttribPointer(texCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0)
    }

    // overrideできると便利そうなので切り出し
    // Uniformをシェーダにバインド
    protected fun graphicPipelineBindUniform(program: Int, inputTexture: Int) {
        val uTextureLocation = GLES20.glGetUniformLocation(program, "u_Texture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture)
        GLES20.glUniform1i(uTextureLocation, 0)
    }

    // endregion GraphicPipeline

    override fun releaseGLObjects() {
        glObjects.release()
    }

    @Throws(PSFilterException::class)
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