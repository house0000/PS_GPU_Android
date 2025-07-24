package com.psgpu.android.gl

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/** assets内のシェーダファイルへのパスを指定すると、その内容のStringを返す。 */
fun readShaderFile(context: Context, shaderSrcAssetsPath: String): String {
    val inputStream = context.assets.open(shaderSrcAssetsPath)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readText()
}