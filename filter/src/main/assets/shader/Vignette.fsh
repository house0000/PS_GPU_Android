#ifdef GL_PRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// ビネットが始まる中心からの距離 (例: 0.3)
uniform float u_vignetteStart;
// ビネットが完全に黒になる距離 (例: 0.75)
uniform float u_vignetteEnd;

// ビネットの中心
uniform vec2 u_center;

// ビネットの色
uniform vec3 u_vignetteColor;

void main() {
    vec2 uv = v_TexCoord;

    // 元のテクスチャから色を取得
    vec4 originalColor = texture2D(u_Texture, uv);

    // 画面中心 (0.5, 0.5) からの距離を計算
    float dist = distance(uv, u_center);

    // smoothstep関数を使い、距離に基づいて輝度係数を計算
    // distがu_vignetteStartより小さい -> 1.0 (明るい)
    // distがu_vignetteEndより大きい   -> 0.0 (暗い)
    // その間は滑らかに補間される
    float vignette = smoothstep(u_vignetteEnd, u_vignetteStart, dist);

    // 元の色に輝度係数を掛けて、最終的な色を決定
    gl_FragColor = vec4(originalColor.rgb * vignette + u_vignetteColor * (1.0 - vignette), originalColor.a);
}