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

// ブラーの中心座標 (UV座標系、中央なら 0.5, 0.5)
uniform vec2 u_blurCenter;

// ブラーの強さ (例: 0.1)
uniform float u_intensity;

// サンプリング回数 (品質。例: 30)
uniform int u_samples;

void main() {
    vec2 uv = v_TexCoord;

    // 色を累積するための変数を初期化
    vec4 totalColor = vec4(0.0);

    // ピクセルからブラーの中心へ向かう方向ベクトルを計算
    vec2 direction = u_blurCenter - uv;

    // サンプリングのステップ（1回あたりの移動距離）を計算
    vec2 step = direction * u_intensity / float(u_samples);

    // 指定された回数だけループしてサンプリング
    for (int i = 0; i < u_samples; i++) {
        // 現在のUV座標にステップを加算してサンプリング位置を決定
        vec2 sampleUv = uv + step * float(i);
        // テクスチャから色を取得し、合計に加える
        totalColor += texture2D(u_Texture, sampleUv);
    }

    // 累積した色をサンプリング回数で割って平均を求める
    gl_FragColor = totalColor / float(u_samples);
}