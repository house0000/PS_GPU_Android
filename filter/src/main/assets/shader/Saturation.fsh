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
uniform float u_saturation;

// RGB色を輝度に変換するための定数
const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

// 彩度は、RGB各値のMaxとMinの差が大きいほど大きいので、その差を調整する。
void main() {
    vec4 textureColor = texture2D(u_Texture, v_TexCoord);

    // グレースケール色を算出
    float luminance = dot(textureColor.rgb, luminanceWeighting);
    vec3 grayColor = vec3(luminance);

    // 彩度調整
    vec3 saturatedColor = mix(grayColor, textureColor.rgb, u_saturation);

    gl_FragColor = vec4(saturatedColor, textureColor.a);
}