#ifdef GL_PRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// 彩度
uniform float u_contrast;

// 彩度は、RGB各値のMaxとMinの差が大きいほど大きいので、その差を調整する。
void main() {
    vec4 textureColor = texture2D(u_Texture, v_TexCoord);

    // 明るい色はより明るく、暗い色はより暗くする。
    vec3 contrastedColor = (textureColor.rgb - 0.5) * u_contrast + 0.5;

    gl_FragColor = vec4(contrastedColor, textureColor.a);
}