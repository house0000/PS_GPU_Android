#ifdef GL_PRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// カーネルサイズ
uniform int u_kernel;

// σ
uniform float u_sigma;

// テクスチャの幅
uniform int u_TextureWidth;

float gaussian(int distance) {
    // (後で加重平均を取るので正規化は不要)
    return exp(- float(distance * distance) / (2.0 * u_sigma * u_sigma));
}

void main() {
    vec4 resultColor = vec4(0.0); // 最終的なピクセルの色
    float totalGaussian = 0.0;  // 重みの合計を記録する変数
    for (int radius = 0; radius <= u_kernel; radius++) {
        if (radius == 0) {
            float gaussian = gaussian(radius);
            totalGaussian += gaussian;

            resultColor += texture2D(u_Texture, v_TexCoord) * gaussian;
        } else {
            float gaussian = gaussian(radius);
            totalGaussian += gaussian * 2.0;

            resultColor +=
                texture2D(u_Texture, v_TexCoord + vec2(float(radius) / float(u_TextureWidth), 0.0)) * gaussian +
                texture2D(u_Texture, v_TexCoord + vec2(- float(radius) / float(u_TextureWidth), 0.0)) * gaussian;
        }
    }

    resultColor = resultColor / totalGaussian;

    gl_FragColor = resultColor;
}