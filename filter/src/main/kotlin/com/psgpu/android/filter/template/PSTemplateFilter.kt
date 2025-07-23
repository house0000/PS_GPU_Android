package com.psgpu.android.filter.template

import android.graphics.Bitmap
import android.opengl.GLES20
import com.psgpu.android.filter.PSFilter
import com.psgpu.android.filter.PSFilterException
import com.psgpu.android.gl.GLES20CreateEmptyTextureObject
import com.psgpu.android.gl.GLES20CreateFBOColorAttachedTexture2D
import com.psgpu.android.gl.GLES20CreateIBO
import com.psgpu.android.gl.GLES20CreateLinkedProgram
import com.psgpu.android.gl.GLES20CreateTextureObject
import com.psgpu.android.gl.GLES20CreateVBO
import com.psgpu.android.gl.GLES20GetTextureUnitSlot
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSGLObjects
import com.psgpu.android.gl.model.PSTextureObject
import com.psgpu.android.gl.readShaderFile
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// シェーダとシェーダ内の変数だけ渡せばレンダリングしてくれるフィルター
// デフォルトはPSNoFilterと同じ挙動
open class PSTemplateFilter(
    protected val params: PSTemplateFilterParams = PSTemplateFilterParams()
): PSFilter {
    // シェーダーソース
    private val vertexShaderSrc = readShaderFile(PSFilter.getContext(), params.vertexShaderSrcPath)
    private val fragmentShaderSrc = readShaderFile(PSFilter.getContext(), params.fragmentShaderSrcPath)


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

    private val vertexCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexCoords.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()
        .apply {
            put(vertexCoords).position(0)
        }
    private val textureCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureCoords.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()
        .apply {
            put(textureCoords).position(0)
        }
    private val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(index.size * 2) // shortは2バイト
        .order(ByteOrder.nativeOrder()).asShortBuffer()
        .apply {
            put(index).position(0)
        }

    private val additionalVertexAttributeBuffers: MutableList<Buffer> = mutableListOf()

    private var glObjects: PSGLObjects = PSGLObjects()


    init {
        params.vertexAttributeParams.forEach { param ->
            when (param) {
                is PSVertexAttributeParam.FloatVector -> {
                    val buffer = ByteBuffer.allocateDirect(param.data.size * Float.SIZE_BYTES)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer()
                        .apply {
                            put(FloatArray(param.data.size) { param.data[it] }).position(0)
                        }
                    additionalVertexAttributeBuffers.add(buffer)
                }
            }
        }
    }

    override fun runGraphicPipeline(
        inputTexture: PSTextureObject,
        width: Int,
        height: Int
    ): PSFBO {
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
        // 頂点座標
        val vertexCoordsVBO =
            GLES20CreateVBO(vertexCoordsBuffer, Float.SIZE_BYTES * vertexCoords.size)
        glObjects.addVBO(vertexCoordsVBO)
        val positionLocation = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexCoordsVBO)
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, 0)
        // 入力テクスチャ座標
        val textureCoordsVBO =
            GLES20CreateVBO(textureCoordsBuffer, Float.SIZE_BYTES * textureCoords.size)
        glObjects.addVBO(textureCoordsVBO)
        val texCoordLocation = GLES20.glGetAttribLocation(program, "a_TexCoord")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordsVBO)
        GLES20.glEnableVertexAttribArray(texCoordLocation)
        GLES20.glVertexAttribPointer(texCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0)
        // カスタム頂点属性
        params.vertexAttributeParams.forEachIndexed { index, param ->
            when (param) {
                is PSVertexAttributeParam.FloatVector -> {
                    val vbo =
                        GLES20CreateVBO(additionalVertexAttributeBuffers[index], Float.SIZE_BYTES * param.data.size)
                    glObjects.addVBO(vbo)
                    val location = GLES20.glGetAttribLocation(program, param.nameOnShader)
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
                    GLES20.glEnableVertexAttribArray(location)
                    GLES20.glVertexAttribPointer(location, param.vecSize, GLES20.GL_FLOAT, false, 0, 0)
                }
            }
        }

        // 頂点のindexを渡す(IBO)
        val ibo = GLES20CreateIBO(indexBuffer, Short.SIZE_BYTES * index.size)
        glObjects.addIBO(ibo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        // --------- グラフィックパイプラインの実行 ---------

        // Uniformを設定する
        // テクスチャは生成だけして最後にまとめてバインドする。
        val textureObjects = mutableListOf<Pair<Int, String>>() // (ハンドラ, 名前)
        // 入力テクスチャ
        textureObjects.add(Pair(inputTexture.handler, "u_Texture"))
        // その他のカスタムUniform
        setupCustomUniformParams(width, height)
        params.uniformParams.forEach { param ->
            when (param) {
                is PSUniformParam.Texture2D -> {
                    // Textureの生成
                    val tex = GLES20CreateTextureObject(
                        param.bitmap,
                        param.minFilter,
                        param.magFilter,
                        param.wrapS,
                        param.wrapT
                    )
                    glObjects.addTexture(tex)
                    textureObjects.add(Pair(tex, param.nameOnShader))
                }

                is PSUniformParam.F1 -> {
                    val location = GLES20.glGetUniformLocation(program, param.nameOnShader)
                    GLES20.glUniform1f(location, param.value)
                }

                is PSUniformParam.F2 -> {
                    val location = GLES20.glGetUniformLocation(program, param.nameOnShader)
                    GLES20.glUniform2f(location, param.valueX, param.valueY)
                }

                is PSUniformParam.F3 -> {
                    val location = GLES20.glGetUniformLocation(program, param.nameOnShader)
                    GLES20.glUniform3f(location, param.x, param.y, param.z)
                }

                is PSUniformParam.I1 -> {
                    val location = GLES20.glGetUniformLocation(program, param.nameOnShader)
                    GLES20.glUniform1i(location, param.value)
                }

                is PSUniformParam.I2 -> {
                    val location = GLES20.glGetUniformLocation(program, param.nameOnShader)
                    GLES20.glUniform2i(location, param.valueX, param.valueY)
                }

            }
        }
        // テクスチャのバインド
        textureObjects.forEachIndexed { index, (tex, name) ->
            val slotIndex = index
            val slot = GLES20GetTextureUnitSlot(slotIndex)
            val location = GLES20.glGetUniformLocation(program, name)
            GLES20.glActiveTexture(slot)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
            GLES20.glUniform1i(location, slotIndex)
        }

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

    // Set custom uniforms
    protected open fun setupCustomUniformParams(width: Int, height: Int) {}

    override fun releaseGLObjects() {
        glObjects.release()
    }

    override fun apply(inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height

        // バリデーションチェック
        // bitmapは有効か
        if (width == 0 || height == 0 || inputBitmap.config == null) {
            throw PSFilterException.InvalideInputBitmap
        }
        params.uniformParams.forEach { param ->
            when (param) {
                is PSUniformParam.Texture2D -> {
                    val bitmap = param.bitmap
                    if (bitmap.width == 0 || bitmap.height == 0 || bitmap.config == null) {
                        throw PSFilterException.InvalideInputBitmap
                    }
                }

                else -> {}
            }
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
