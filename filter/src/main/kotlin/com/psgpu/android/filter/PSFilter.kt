package com.psgpu.android.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.psgpu.android.gl.EGL14SetupContext
import com.psgpu.android.gl.GLES20CreateTextureObject
import com.psgpu.android.gl.PSGLException
import com.psgpu.android.gl.model.PSEGLContextObjects
import com.psgpu.android.gl.model.PSFBO
import com.psgpu.android.gl.model.PSTextureObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.createBitmap

/** フィルターのインターフェース */
interface PSFilter {
    companion object {
        fun init(applicationContext: Context) {
            context = { applicationContext }
        }

        // Android Context
        private var context: () -> Context = {
            throw Exception()
        }

        fun getContext(): Context = context()

        private var eglContext: PSEGLContextObjects? = null
        protected val eglContextExist: Boolean
            get() {
                return eglContext != null
            }
    }

    // region Rendering Flow

    // Setup
    fun enableDrawContext() {
        if (!eglContextExist) {
            eglContext = try {
                EGL14SetupContext()
            } catch (e: PSGLException) {
                null
            }
        }
    }

    //Create input bitmap texture
    fun createInputTexture(inputBitmap: Bitmap): PSTextureObject {
        return PSTextureObject(GLES20CreateTextureObject(inputBitmap))
    }

    // Create filtered texture
    fun runGraphicPipeline(
        inputTexture: PSTextureObject,
        width: Int,
        height: Int
    ): PSFBO

    // Create output bitmap
    @Throws(PSFilterException::class)
    fun createOutputBitmap(
        outputFBO: PSFBO,
        width: Int,
        height: Int
    ): Bitmap {
        if (width <= 0 || height <= 0) {
            throw PSFilterException.CreateOutputBitmapInvalidBitmapSize
        }

        // FBOから出力データをBuffer形式で取得
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFBO.handler)
        val byteBuffer = ByteBuffer.allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(
            0,
            0,
            width,
            height,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            byteBuffer
        )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        // Bitmapに変換
        val outputBitmap = createBitmap(width, height)
        outputBitmap.copyPixelsFromBuffer(byteBuffer)

        return outputBitmap
    }

    fun releaseGLObjects()

    fun releaseDrawContext() {
        eglContext?.release()
        eglContext = null
    }

    // endregion Rendering Flow

    /**
     * Create filtered bitmap
     * */
    @Throws(PSFilterException::class)
    fun apply(inputBitmap: Bitmap): Bitmap
}