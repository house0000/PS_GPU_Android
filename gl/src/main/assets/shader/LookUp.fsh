#ifdef GL_PRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

// テクスチャ座標
varying vec2 v_TexCoord;

// テクスチャユニット
uniform sampler2D u_Texture;

// LUT画像
uniform sampler2D u_lookupTexture;

// 強度
uniform float u_intensity;

// tiles per one line on LUT.
uniform int u_gridSize;

// pixels per one line on a tile of LUT.
uniform int u_tileSize;

void main() {
    // 元の画像から色を取得
    vec4 sourceColor = texture2D(u_Texture, v_TexCoord);

    // Blueチャンネルをタイルの総数-1の範囲にマッピング
    float blueSlice = sourceColor.b * (float(u_tileSize) - 1.0);

    float slice1 = floor(blueSlice);
    float slice2 = ceil(blueSlice);
    float blue_interp = fract(blueSlice);

    // 1つ目のUV座標を計算
    float u1 = (mod(slice1, float(u_gridSize)) + sourceColor.r) / float(u_gridSize);
    float v1 = (floor(slice1 / float(u_gridSize)) + sourceColor.g) / float(u_gridSize);
    vec2 uv1 = vec2(u1, v1);

    // 2つ目のUV座標を計算
    float u2 = (mod(slice2, float(u_gridSize)) + sourceColor.r) / float(u_gridSize);
    float v2 = (floor(slice2 / float(u_gridSize)) + sourceColor.g) / float(u_gridSize);
    vec2 uv2 = vec2(u2, v2);

    // LUT画像から2つの新しい色をサンプリング
    vec4 color1 = texture2D(u_lookupTexture, uv1);
    vec4 color2 = texture2D(u_lookupTexture, uv2);

    // 2つの色を混ぜ合わせ、最終的なLUTカラーを決定
    vec4 lutColor = mix(color1, color2, blue_interp);

    // 元の色とLUTカラーを、指定された強度で混ぜ合わせる
    gl_FragColor = mix(sourceColor, lutColor, u_intensity);
}