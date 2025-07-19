#ifdef GL_PRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif

// 頂点シェーダから渡されるテクスチャ座標
varying vec2 v_TexCoord;

// C++側から設定するテクスチャユニット
uniform sampler2D u_Texture;

void main() {
    gl_FragColor = texture2D(u_Texture, v_TexCoord);
}