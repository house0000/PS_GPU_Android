#ifdef GL_PRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

void main() {
    gl_FragColor = texture2D(u_Texture, v_TexCoord);
}