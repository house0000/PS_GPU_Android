package com.psgpu.android.filter

import com.psgpu.android.gl.PSGLException

sealed class PSFilterException: Exception() {
    data class GL(val glException: PSGLException): PSFilterException()
    data object InvalideInputBitmap: PSFilterException()
    data object InvalidFBOStatus: PSFilterException()
    data object CreateOutputBitmapInvalidBitmapSize: PSFilterException()
}