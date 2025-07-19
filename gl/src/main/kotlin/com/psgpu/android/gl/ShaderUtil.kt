package com.psgpu.android.gl

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun readShaderFile(context: Context, shaderSrcPath: String): String {
    val inputStream = context.assets.open(shaderSrcPath)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readText()
}