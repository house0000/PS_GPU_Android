package com.psgpu.android

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psgpu.android.ui.theme.PSGPU_androidTheme
import com.psgpu.android.filter.PSFilterType
import com.psgpu.android.ui.filter.FilterItem
import com.psgpu.android.ui.filter.FilterItemState

data class MainState(
    val bitmap: Bitmap? = null,
    val selectedFilter: PSFilterType? = null,
    val filters: List<FilterItemState> = emptyList()
)

sealed class MainEvent {
    data object ResetFilter: MainEvent()
    sealed class ApplyFilter(open val filterType: PSFilterType): MainEvent() {
        data class NoParameterFilter(override val filterType: PSFilterType): ApplyFilter(filterType)
        data class GaussianBlur(val radius: Float? = null, val sigma: Float? = null): ApplyFilter(PSFilterType.GAUSSIAN_BLUR)
        data class Sharpen(val intensity: Float): ApplyFilter(PSFilterType.SHARPEN)
        data class Saturation(val saturation: Float): ApplyFilter(PSFilterType.SATURATION)
        data class Contrast(val contrast: Float): ApplyFilter(PSFilterType.CONTRAST)
        data class ZoomBlur(
            val intensity: Float? = null,
            // (0.5f, 0.5f) means center.
            val blurCenter: Pair<Float?, Float?>? = null,
            // sampling count. (Quality - Calculation trade-off)
            val samples: Int? = null,
            val sigma: Float? = null
        ): ApplyFilter(PSFilterType.ZOOM_BLUR)
        data class Vignette(
            val vignetteStart: Float? = null,
            val vignetteEnd: Float? = null,
            val center: Pair<Float?, Float?>? = null,
            val colorRGB: Triple<Float?, Float?, Float?>? = null
        ): ApplyFilter(PSFilterType.VIGNETTE)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        enableEdgeToEdge()
        setContent {
            PSGPU_androidTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val onEvent = viewModel::onEvent

                Screen(state, onEvent)
            }
        }
    }
}

@Composable
private fun Screen(
    state: MainState,
    onEvent: (MainEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // ビットマップ画像
        BitmapImage(state.bitmap, state.selectedFilter)

        // リセットボタン
        ResetButton {
            onEvent(MainEvent.ResetFilter)
        }

        // フィルター一覧
        FilterList(state.filters, selectedFilter = state.selectedFilter, onEvent)

        // ナビゲーションバーのスペース
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
}

@Composable
private fun ColumnScope.BitmapImage(
    bitmap: Bitmap?,
    selectedFilter: PSFilterType?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .weight(5.5f)
    ) {
        // ビットマップイメージ
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .weight(1f)
            ) {
                Text("No Bitmap Error", color = Color.Black)
            }
        }

        // 選択中のフィルター
        Text(
            "filter: ${selectedFilter?.title ?: "none"}",
            modifier = Modifier
                .padding(12.dp)
        )
    }
}

@Composable
private fun ColumnScope.ResetButton(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.5f)
    ) {
        Text("Reset")
    }
}

@Composable
private fun ColumnScope.FilterList(
    filters: List<FilterItemState>,
    selectedFilter: PSFilterType?,
    onEvent: (MainEvent) -> Unit
) {
    Text("filters:", Modifier.fillMaxWidth())
    Column(
        modifier = Modifier
           .verticalScroll(rememberScrollState())
           .weight(4f)
           .fillMaxWidth()
    ) {
        filters.forEach { filter ->
            FilterItem(state = filter, selected = selectedFilter == filter.filter)
        }
    }
}