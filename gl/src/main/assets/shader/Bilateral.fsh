#ifdef GL_PRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// テクスチャの解像度
uniform ivec2 u_TextureResolution;

uniform float u_spatialSigma;     // 空間距離の強さ (例: 4.0)
uniform float u_colorSigma;       // 色の類似度の強さ (例: 0.1)
uniform int u_radius;             // サンプリング範囲の半径 (例: 4)

// ガウス関数: 重みを計算するために使用
float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma));
}

void main() {
    vec2 uv = v_TexCoord;
    vec2 texelSize = vec2(1.0) / vec2(u_TextureResolution);

    // 処理の中心となるピクセルの色を取得
    vec4 centerColor = texture2D(u_Texture, uv);

    // 最終的な色と重みの合計を初期化
    vec4 totalColor = vec4(0.0);
    float totalWeight = 0.0;

    // u_radiusで指定された範囲のピクセルをループ処理
    for (int x = -u_radius; x <= u_radius; x++) {
        for (int y = -u_radius; y <= u_radius; y++) {
            // 近隣ピクセルの座標と色を取得
            vec2 offset = vec2(float(x), float(y));
            vec2 neighborUv = uv + offset * texelSize;
            vec4 neighborColor = texture2D(u_Texture, neighborUv);

            // 1. 空間的な距離に基づく重みを計算
            float spatialDist = length(offset);
            float spatialWeight = gaussian(spatialDist, u_spatialSigma);

            // 2. 色の類似度に基づく重みを計算
            float colorDist = distance(centerColor.rgb, neighborColor.rgb);
            float colorWeight = gaussian(colorDist, u_colorSigma);

            // 最終的な重みは、2つの重みの積
            float weight = spatialWeight * colorWeight;

            // 重みをかけた色と重み自体をそれぞれ加算
            totalColor += neighborColor * weight;
            totalWeight += weight;
        }
    }

    // 加重平均を計算して最終的な色を決定
    gl_FragColor = totalColor / totalWeight;
}