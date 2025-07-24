package com.psgpu.android.gl

sealed class PSGLException: Exception() {
    data object EGLNoDisplay: PSGLException() {
        private fun readResolve(): Any = EGLNoDisplay
    }

    data object EGLInitializeFailed: PSGLException() {
        private fun readResolve(): Any = EGLInitializeFailed
    }

    data object EGLGetConfigFailed: PSGLException() {
        private fun readResolve(): Any = EGLGetConfigFailed
    }

    data object EGLNoSurface: PSGLException() {
        private fun readResolve(): Any = EGLNoSurface
    }

    data object EGLNoContext: PSGLException() {
        private fun readResolve(): Any = EGLNoContext
    }

    data object EGLMakeContextCurrentFailed: PSGLException() {
        private fun readResolve(): Any = EGLMakeContextCurrentFailed
    }

    data object CreateShaderFailed: PSGLException() {
        private fun readResolve(): Any = CreateShaderFailed
    }

    data object CompileShaderFailed: PSGLException() {
        private fun readResolve(): Any = CompileShaderFailed
    }

    data object LinkProgramFailed: PSGLException() {
        private fun readResolve(): Any = LinkProgramFailed
    }

    data object TextureOriginalBitmapHasInvalidConfig: PSGLException() {
        private fun readResolve(): Any = TextureOriginalBitmapHasInvalidConfig
    }

    data object GetTextureAttachedToFBOFailed: PSGLException() {
        private fun readResolve(): Any = GetTextureAttachedToFBOFailed
    }
}