package com.psgpu.android.filter

import com.psgpu.android.filter.gl.PSGLException

/** フィルターのエラー */
sealed class PSFilterException: Exception() {
    data class GL(val glException: PSGLException): PSFilterException()
    data object InvalideInputBitmap: PSFilterException() {
        private fun readResolve(): Any = InvalideInputBitmap
    }

    data object InvalidFBOStatus: PSFilterException() {
        private fun readResolve(): Any = InvalidFBOStatus
    }

    data object CreateOutputBitmapInvalidBitmapSize: PSFilterException() {
        private fun readResolve(): Any = CreateOutputBitmapInvalidBitmapSize
    }
}