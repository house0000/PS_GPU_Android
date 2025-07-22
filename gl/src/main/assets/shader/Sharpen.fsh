#ifdef GL_PRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// 強度
uniform float u_intensity;

// テクスチャの幅
uniform int u_TextureWidth;

// テクスチャの高さ
uniform int u_TextureHeight;

void main() {
    float pixelWidth = 1.0 / float(u_TextureWidth);
    float pixelHeight = 1.0 / float(u_TextureHeight);

    vec4 center = texture2D(u_Texture, v_TexCoord);
    vec4 left = texture2D(u_Texture, v_TexCoord - vec2(pixelWidth, 0.0));
    vec4 right = texture2D(u_Texture, v_TexCoord + vec2(pixelWidth, 0.0));
    vec4 top = texture2D(u_Texture, v_TexCoord - vec2(0.0, pixelHeight));
    vec4 bottom = texture2D(u_Texture, v_TexCoord + vec2(0.0, pixelHeight));

    float centerMultiplier = 1.0 + 4.0 * u_intensity;
    float edgeMultiplier = u_intensity;

    vec4 shapenColor = vec4((center * centerMultiplier - (left + right + top + bottom) * edgeMultiplier).rgb, center.a);

    gl_FragColor = shapenColor;
}