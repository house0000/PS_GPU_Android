// 頂点座標
attribute vec4 a_Position;

// テクスチャ座標
attribute vec2 a_TexCoord;

// テクスチャ座標(varying)
varying vec2 v_TexCoord;

void main() {
    gl_Position = a_Position;
    v_TexCoord = a_TexCoord;
}