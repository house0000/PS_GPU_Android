package com.psgpu.android.gl

sealed class PSGLException: Exception() {
    data object EGLNoDisplay: PSGLException()
    data object EGLInitializeFailed: PSGLException()
    data object EGLGetConfigFailed: PSGLException()
    data object EGLNoSurface: PSGLException()
    data object EGLNoContext: PSGLException()
    data object EGLMakeContextCurrentFailed: PSGLException()
    data object CreateShaderFailed: PSGLException()
    data object CompileShaderFailed: PSGLException()
    data object LinkProgramFailed: PSGLException()
    data object TextureOriginalBitmapHasInvalidConfig: PSGLException()
    data object GetTextureAttachedToFBOFailed: PSGLException()
}