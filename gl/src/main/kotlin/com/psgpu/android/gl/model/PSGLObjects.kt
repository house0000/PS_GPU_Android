package com.psgpu.android.gl.model

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import java.nio.IntBuffer

/**
 * GL関連のインスタンスは手動でリリースしないといけないので、リリースを担ってくれるオブジェクトを作っておく。
 */
interface PSGLObject {
    fun release()
}

/** GL関連のインスタンスをまとめて格納しておけて、まとめてリリース処理できる箱 */
data class PSGLObjects(
    var program: PSGLProgramObjects? = null,
    val textureObjects: MutableList<PSTextureObject> = mutableListOf(),
    val vbos: MutableList<PSVBO> = mutableListOf(),
    val ibos: MutableList<PSIBO> = mutableListOf(),
    val fbos: MutableList<PSFBO> = mutableListOf()
) : PSGLObject {

    fun addProgram(program: PSGLProgramObjects) {
        this.program = program
    }

    fun addTexture(handler: Int) {
        textureObjects.add(PSTextureObject(handler))
    }

    fun addVBO(handler: Int) {
        vbos.add(PSVBO(handler))
    }

    fun addIBO(handler: Int) {
        ibos.add(PSIBO(handler))
    }

    fun addFBO(handler: Int) {
        fbos.add(PSFBO(handler))
    }

    override fun release() {
        fbos.forEach { it.release() }
        vbos.forEach { it.release() }
        ibos.forEach { it.release() }
        textureObjects.forEach { it.release() }
        program?.release()
    }

}

/** プログラム用のリリースコンテナ */
data class PSGLProgramObjects(
    var vertexShader: Int? = null,
    var fragmentShader: Int? = null,
    var program: Int? = null
): PSGLObject {
    override fun release() {
        program?.let {
            GLES20.glDeleteProgram(it)
        }
        vertexShader?.let {
            GLES20.glDeleteShader(it)
        }
        fragmentShader?.let {
            GLES20.glDeleteShader(it)
        }
    }
}

/** VBO用のリリースコンテナ */
data class PSVBO(
    val handler: Int
): PSGLObject {
    override fun release() {
        GLES20.glDeleteBuffers(1, intArrayOf(handler), 0)
    }
}

/** IBO用のリリースコンテナ */
data class PSIBO(
    val handler: Int
): PSGLObject {
    override fun release() {
        GLES20.glDeleteBuffers(1, intArrayOf(handler), 0)
    }
}

/** FBO用のリリースコンテナ */
data class PSFBO(
    val handler: Int
): PSGLObject {
    override fun release() {
        GLES20.glDeleteFramebuffers(1, intArrayOf(handler), 0)
    }
}

/** テクスチャ用のリリースコンテナ */
data class PSTextureObject(
    val handler: Int
): PSGLObject {
    override fun release() {
        GLES20.glDeleteTextures(1, intArrayOf(handler), 0)
    }
}

/** EGL初期化オブジェクト用のリリースコンテナ */
data class PSEGLContextObjects(
    var display: EGLDisplay? = null,
    var config: EGLConfig? = null,
    var surface: EGLSurface? = null,
    var context: EGLContext? = null
): PSGLObject {
    override fun release() {
        // コンテキストを現在のスレッドから切り離す
        if (context != null && display != null)
            EGL14.eglMakeCurrent(
                display,
                EGL14.EGL_NO_SURFACE, // どのサーフェスにも接続しない
                EGL14.EGL_NO_SURFACE, // どのサーフェスにも接続しない
                EGL14.EGL_NO_CONTEXT  // どのコンテキストにも接続しない
            )

        display?.let {
            // サーフェスとコンテキストを破棄する
            surface?.let {
                EGL14.eglDestroySurface(display, surface)
            }
            context?.let {
                EGL14.eglDestroyContext(display, context)
            }

            // ディスプレイ接続を終了する
            EGL14.eglTerminate(display)
        }
    }
}