package com.psgpu.android.filter.gl

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import androidx.annotation.IntRange
import com.psgpu.android.filter.gl.model.PSEGLContextObjects
import com.psgpu.android.filter.gl.model.PSGLProgramObjects
import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * gl環境のセットアップ。
 * やってること: 有効なコンテキストがないとOpenGLシステムが反応してくれないので、使わないけど適当なEGLオフスクリーンレンダリング環境を立ち上げておき、有効なContextを作成する。
 */
@Throws(PSGLException::class)
fun EGL14SetupContext(): PSEGLContextObjects {
    val objects = PSEGLContextObjects()

    // ディスプレイの生成
    val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
        throw PSGLException.EGLNoDisplay
    }
    objects.display = eglDisplay

    // EGL初期化
    val version = IntArray(2)
    if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
        throw PSGLException.EGLInitializeFailed
    }

    // pBufferレンダリングに使える設定(EGLConfig)を取得する。
    val attribList = intArrayOf(
        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL14.EGL_RED_SIZE, 8,
        EGL14.EGL_GREEN_SIZE, 8,
        EGL14.EGL_BLUE_SIZE, 8,
        EGL14.EGL_ALPHA_SIZE, 8,
        EGL14.EGL_NONE
    )
    val configs = arrayOfNulls<EGLConfig>(1)
    val numConfigs = IntArray(1)
    if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0) || configs[0] == null) {
        throw PSGLException.EGLGetConfigFailed
    }
    val eglConfig = configs[0]
    objects.config = eglConfig

    // オフスクリーンサーフェスの取得
    val pbufferAttribs = intArrayOf(
        EGL14.EGL_LARGEST_PBUFFER, EGL14.EGL_TRUE,
        EGL14.EGL_NONE
    )
    val eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, pbufferAttribs, 0)
    if (eglSurface == EGL14.EGL_NO_SURFACE) {
        throw PSGLException.EGLNoSurface
    }
    objects.surface = eglSurface

    // コンテキスの取得
    val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
    val eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
    if (eglContext == EGL14.EGL_NO_CONTEXT) {
        throw PSGLException.EGLNoContext
    }
    objects.context = eglContext

    if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
        throw PSGLException.EGLMakeContextCurrentFailed
    }

    return objects
}

/** コンパイル済みのシェーダオブジェクトを作成し、ハンドルを返す。 */
@Throws(PSGLException::class)
fun GLES20LoadShader(type: Int, shaderSrc: String): Int {
    val shader = GLES20.glCreateShader(type)
    if (shader == 0) {
        throw PSGLException.CreateShaderFailed
    }

    GLES20.glShaderSource(shader, shaderSrc)

    GLES20.glCompileShader(shader)

    val compiled = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        GLES20.glDeleteShader(shader)
        throw PSGLException.CompileShaderFailed
    }

    return shader
}

/** リンク済みのプログラムオブジェクトを作成し、そのハンドルを返す */
@Throws(PSGLException::class)
fun GLES20CreateLinkedProgram(
    vertexShaderSrc: String,
    fragmentShaderSrc: String
): PSGLProgramObjects {
    val objects = PSGLProgramObjects()

    try {
        val program = GLES20.glCreateProgram()
        objects.program = program

        val vertexShader = GLES20LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc)
        val fragmentShader = GLES20LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc)
        objects.vertexShader = vertexShader
        objects.fragmentShader = fragmentShader

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)

        GLES20.glLinkProgram(program)

        val linked = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            throw PSGLException.LinkProgramFailed
        }

        return objects
    } catch (e: PSGLException) {
        objects.release()
        throw e
    }
}

/** ビットマップからテクスチャオブジェクトを作成し、そのハンドルを返す。 */
@Throws(PSGLException::class)
fun GLES20CreateTextureObject(
    bitmap: Bitmap,
    minFilter: Int = GLES20.GL_LINEAR,
    magFilter: Int = GLES20.GL_LINEAR,
    wrapS: Int = GLES20.GL_CLAMP_TO_EDGE,
    wrapT: Int = GLES20.GL_CLAMP_TO_EDGE
): Int {
    // Bitmapの情報
    if (bitmap.config == null) {
        throw PSGLException.TextureOriginalBitmapHasInvalidConfig
    }

    // テクスチャの生成
    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    val textureId = textures[0]

    // テクスチャのバインド
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

    // テクスチャのロード
    val buffer = ByteBuffer.allocate(bitmap.byteCount)
    bitmap.copyPixelsToBuffer(buffer)
    buffer.position(0)
    // Textureの形式はRGBA32ビットに統一してその中に格納する。(Bitmapのconfigに合わせて変えると各所での設定が面倒そうなので。)
    val format = GLES20.GL_RGBA // ピクセルはRGBAを持ち
    val dataType = GLES20.GL_UNSIGNED_BYTE // チャンネルあたり8ビット

    GLES20.glTexImage2D(
        GLES20.GL_TEXTURE_2D,
        0, // mipmap level
        format,
        bitmap.width, bitmap.height,
        0, // border (should be 0 in OpenGLES)
        format,
        dataType,
        buffer
    )

    // ミップマップ生成(一旦使わないので保留)

    // テクスチャのサンプリング設定
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT)

    // テクスチャのアンバインド
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    return textureId
}

/** 空のテクスチャオブジェクトを生成し、ハンドルを返す */
fun GLES20CreateEmptyTextureObject(
    width: Int,
    height: Int,
    minFilter: Int = GLES20.GL_LINEAR,
    magFilter: Int = GLES20.GL_LINEAR,
    wrapS: Int = GLES20.GL_CLAMP_TO_EDGE,
    wrapT: Int = GLES20.GL_CLAMP_TO_EDGE
): Int {
    // テクスチャの生成
    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    val textureId = textures[0]

    // テクスチャのバインド
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

    // テクスチャのロード
    // Textureの形式はRGBA32ビットに統一してその中に格納する。(Bitmapのconfigに合わせて変えると各所での設定が面倒そうなので。)
    val format = GLES20.GL_RGBA // ピクセルはRGBAを持ち
    val dataType = GLES20.GL_UNSIGNED_BYTE // チャンネルあたり8ビット

    GLES20.glTexImage2D(
        GLES20.GL_TEXTURE_2D,
        0, // mipmap level
        format,
        width, height,
        0, // border (should be 0 in OpenGLES)
        format,
        dataType,
        null
    )

    // テクスチャのサンプリング設定
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT)

    // テクスチャのアンバインド
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    return textureId
}

/** FBOを生成し、指定したテクスチャをカラーアタッチメントにバインドしてから、ハンドルを返す。 */
fun GLES20CreateFBOColorAttachedTexture2D(textureObject: Int): Int {
    // 生成
    val fbos = IntArray(1)
    GLES20.glGenFramebuffers(1, fbos, 0)
    val fbo = fbos[0]

    // バインド
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo)

    // アタッチ
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureObject, 0)

    // アンバインド
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

    return fbo
}

/** 頂点属性からVBOを作成し、ハンドルを返す。 */
fun GLES20CreateVBO(
    data: Buffer,
    byteSize: Int
): Int {
    // 生成
    val vbos = IntArray(1)
    GLES20.glGenBuffers(1, vbos, 0)
    val vbo = vbos[0]

    // バインド
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)

    // データを流し込む
    GLES20.glBufferData(
        GLES20.GL_ARRAY_BUFFER,
        byteSize,
        data,
        GLES20.GL_STATIC_DRAW
    )

    // アンバインド
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    return vbo
}

/** index配列からIBOを作成しハンドルを返す。 */
fun GLES20CreateIBO(
    data: Buffer,
    byteSize: Int
): Int {
    // 生成
    val ibos = IntArray(1)
    GLES20.glGenBuffers(1, ibos, 0)
    val ibo = ibos[0]

    // バインド
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

    // データを流し込む
    GLES20.glBufferData(
        GLES20.GL_ELEMENT_ARRAY_BUFFER,
        byteSize,
        data,
        GLES20.GL_STATIC_DRAW
    )

    // アンバインド
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    return ibo
}

/**
 * 現在バインドされているFBOのカラーアタッチメント0に接続されたテクスチャのIDを取得します。
 *
 * @return 成功した場合はテクスチャID、何もアタッチされていないかエラーの場合は0を返します。
 */
@Throws(PSGLException::class)
fun GLES20GetTextureAttachedToFBO(fbo: Int): Int {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo)

    // 戻り値を受け取るためのIntArray (サイズ1)
    val params = IntArray(1)

    // FBOのカラーアタッチメント0から情報を取得する
    GLES20.glGetFramebufferAttachmentParameteriv(
        GLES20.GL_FRAMEBUFFER,                 // ターゲット: 現在のフレームバッファ
        GLES20.GL_COLOR_ATTACHMENT0,          // 問い合わせるアタッチメントポイント
        GLES20.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, // 取得したい情報: アタッチされたオブジェクトの名前(ID)
        params,                               // 結果を格納する配列
        0                                     // 配列のオフセット
    )

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

    // エラーチェック (任意だが推奨)
    val error = GLES20.glGetError()
    if (error != GLES20.GL_NO_ERROR) {
        throw PSGLException.GetTextureAttachedToFBOFailed
    }

    // 配列の0番目に格納されたテクスチャIDを返す
    return params[0]
}

/** Intをテクスチャユニットに変換する便利関数 */
fun GLES20GetTextureUnitSlot(@IntRange(from = 0, to = 31) index: Int): Int {
    return when (index) {
        0 -> GLES20.GL_TEXTURE0
        1 -> GLES20.GL_TEXTURE1
        2 -> GLES20.GL_TEXTURE2
        3 -> GLES20.GL_TEXTURE3
        4 -> GLES20.GL_TEXTURE4
        5 -> GLES20.GL_TEXTURE5
        6 -> GLES20.GL_TEXTURE6
        7 -> GLES20.GL_TEXTURE7
        8 -> GLES20.GL_TEXTURE8
        9 -> GLES20.GL_TEXTURE9
        10 -> GLES20.GL_TEXTURE10
        11 -> GLES20.GL_TEXTURE11
        12 -> GLES20.GL_TEXTURE12
        13 -> GLES20.GL_TEXTURE13
        14 -> GLES20.GL_TEXTURE14
        15 -> GLES20.GL_TEXTURE15
        16 -> GLES20.GL_TEXTURE16
        17 -> GLES20.GL_TEXTURE17
        18 -> GLES20.GL_TEXTURE18
        19 -> GLES20.GL_TEXTURE19
        20 -> GLES20.GL_TEXTURE20
        21 -> GLES20.GL_TEXTURE21
        22 -> GLES20.GL_TEXTURE22
        23 -> GLES20.GL_TEXTURE23
        24 -> GLES20.GL_TEXTURE24
        25 -> GLES20.GL_TEXTURE25
        26 -> GLES20.GL_TEXTURE26
        27 -> GLES20.GL_TEXTURE27
        28 -> GLES20.GL_TEXTURE28
        29 -> GLES20.GL_TEXTURE29
        30 -> GLES20.GL_TEXTURE30
        31 -> GLES20.GL_TEXTURE31
        else -> -1
    }
}