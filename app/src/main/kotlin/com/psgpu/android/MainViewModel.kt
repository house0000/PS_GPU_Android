package com.psgpu.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.psgpu.android.filter.PSContrastFilter
import com.psgpu.android.filter.PSFilter
import com.psgpu.android.filter.PSFilterException
import com.psgpu.android.filter.PSFilterType
import com.psgpu.android.filter.PSGaussianBlurFilter
import com.psgpu.android.filter.PSNoFilter
import com.psgpu.android.filter.PSSaturationFilter
import com.psgpu.android.filter.PSSharpenFilter
import com.psgpu.android.filter.PSVignetteFilter
import com.psgpu.android.filter.PSZoomBlurFilter
import com.psgpu.android.filter.template.PSTemplateFilter
import com.psgpu.android.ui.filter.FilterItemState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.max

sealed class MainMessage {

}

class MainViewModel(
    private val applicationContext: Context
): ViewModel(), DefaultLifecycleObserver {
    class Factory(private val applicationContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val defaultBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test_image)
    }

    private val filters: Map<PSFilterType, PSFilter> = allFilters()

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState>
        get() = _state.asStateFlow()

    private val _message = MutableSharedFlow<MainMessage>()
    val message: SharedFlow<MainMessage>
        get() = _message.asSharedFlow()

    init {
        setup()
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ApplyFilter -> {
                // 選択されたフィルターを適用する
                val filterType = event.filterType

                val filteredBitmap =  when (event) {
                    is MainEvent.ApplyFilter.GaussianBlur -> {
                        val filter = filters[filterType] as PSGaussianBlurFilter
                        filter.setParams(event.radius?.toInt(), event.sigma)
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }
                    is MainEvent.ApplyFilter.NoParameterFilter -> {
                        val filter = filters[filterType]!!
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }

                    is MainEvent.ApplyFilter.Sharpen -> {
                        val filter = filters[filterType] as PSSharpenFilter
                        filter.setParams(event.intensity)
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }

                    is MainEvent.ApplyFilter.Saturation -> {
                        val filter = filters[filterType] as PSSaturationFilter
                        filter.setParams(event.saturation)
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }

                    is MainEvent.ApplyFilter.Contrast -> {
                        val filter = filters[filterType] as PSContrastFilter
                        filter.setParams(event.contrast)
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }

                    is MainEvent.ApplyFilter.ZoomBlur -> {
                        val filter = filters[filterType] as PSZoomBlurFilter
                        filter.setParams(
                            event.intensity,
                            event.blurCenter,
                            event.samples
                        )
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }

                    is MainEvent.ApplyFilter.Vignette -> {
                        val filter = filters[filterType] as PSVignetteFilter
                        val currentColor = filter.getVignetteColor().let { colorInt ->
                            android.graphics.Color.valueOf(colorInt)
                        }
                        filter.setParams(
                            event.vignetteStart,
                            event.vignetteEnd,
                            event.center,
                            android.graphics.Color.argb(
                                1f,
                                event.colorRGB?.first ?: currentColor.red(),
                                event.colorRGB?.second ?: currentColor.green(),
                                event.colorRGB?.third ?: currentColor.blue()
                            )
                        )
                        try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "ApplyFilter error: $e")
                            Bitmap.createBitmap(defaultBitmap.width, defaultBitmap.height, Bitmap.Config.ARGB_8888)
                        }
                    }
                }

                _state.update {
                    it.copy(
                        bitmap = filteredBitmap,
                        selectedFilter = filterType
                    )
                }
            }
            MainEvent.ResetFilter -> {
                _state.update {
                    it.copy(
                        bitmap = defaultBitmap,
                        selectedFilter = null
                    )
                }
            }
        }
    }

    private fun setup() {
        _state.update {
            it.copy(
                bitmap = defaultBitmap,
                filters = PSFilterType.entries.map { filterType ->
                    FilterItemState(
                        filter = filterType,
                        onSelect = {
                            when (filterType) {
                                PSFilterType.GAUSSIAN_BLUR -> {
                                    onEvent(MainEvent.ApplyFilter.GaussianBlur())
                                }
                                else -> {
                                    onEvent(MainEvent.ApplyFilter.NoParameterFilter(filterType))
                                }
                            }
                        },
                        sliders = when (filterType) {
                            PSFilterType.GAUSSIAN_BLUR -> listOf(
                                FilterItemState.SliderState(
                                    title = "radius",
                                    min = 0f,
                                    max = 100f,
                                    onSlide = { radius ->
                                        onEvent(MainEvent.ApplyFilter.GaussianBlur(radius = radius))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "sigma",
                                    min = 0.5f,
                                    max = 100f,
                                    onSlide = { sigma ->
                                        onEvent(MainEvent.ApplyFilter.GaussianBlur(sigma = sigma))
                                    }
                                )
                            )
                            PSFilterType.SHARPEN -> listOf(
                                FilterItemState.SliderState(
                                    title = "intensity",
                                    min = -10f,
                                    max = 10f,
                                    onSlide = { intensity ->
                                        onEvent(MainEvent.ApplyFilter.Sharpen(intensity = intensity))
                                    }
                                ),
                            )
                            PSFilterType.SATURATION -> listOf(
                                FilterItemState.SliderState(
                                    title = "saturation",
                                    min = 0f,
                                    max = 3f,
                                    onSlide = { saturation ->
                                        onEvent(MainEvent.ApplyFilter.Saturation(saturation))
                                    }
                                ),
                            )

                            PSFilterType.CONTRAST -> listOf(
                                FilterItemState.SliderState(
                                    title = "contrast",
                                    min = 0f,
                                    max = 3f,
                                    onSlide = { contrast ->
                                        onEvent(MainEvent.ApplyFilter.Contrast(contrast))
                                    }
                                ),
                            )

                            PSFilterType.ZOOM_BLUR -> listOf(
                                FilterItemState.SliderState(
                                    title = "intensity",
                                    min = 0f,
                                    max = 1f,
                                    onSlide = { intensity ->
                                        onEvent(MainEvent.ApplyFilter.ZoomBlur(intensity = intensity))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "centerX",
                                    min = 0f,
                                    max = 1f,
                                    onSlide = { centerX ->
                                        onEvent(MainEvent.ApplyFilter.ZoomBlur(blurCenter = Pair(centerX, null)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "centerY",
                                    min = 0f,
                                    max = 1f,
                                    onSlide = { centerY ->
                                        onEvent(MainEvent.ApplyFilter.ZoomBlur(blurCenter = Pair(null, centerY)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "samples",
                                    min = 1f,
                                    max = 30f,
                                    onSlide = { samples ->
                                        onEvent(MainEvent.ApplyFilter.ZoomBlur(samples = samples.toInt()))
                                    }
                                )
                            )

                            PSFilterType.VIGNETTE -> listOf(
                                FilterItemState.SliderState(
                                    title = "vignette start",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { start ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(vignetteStart = start))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "vignette end",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { end ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(vignetteEnd = end))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "center x",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { x ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(center = Pair(x, null)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "center y",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { y ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(center = Pair(null, y)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "R",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { r ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(colorRGB = Triple(r, null, null)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "G",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { g ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(colorRGB = Triple(null, g, null)))
                                    }
                                ),
                                FilterItemState.SliderState(
                                    title = "B",
                                    min = 0f,
                                    max = 1.0f,
                                    onSlide = { b ->
                                        onEvent(MainEvent.ApplyFilter.Vignette(colorRGB = Triple(null, null, b)))
                                    }
                                )
                            )
                            else -> emptyList()
                        }
                    )
                }
            )
        }
    }
}

private fun allFilters(): Map<PSFilterType, PSFilter> = PSFilterType.entries
    .let { types ->
        val map = mutableMapOf<PSFilterType, PSFilter>()

        types.forEach { type ->
            map[type] = when (type) {
                PSFilterType.NO_FILTER -> PSNoFilter()
                PSFilterType.GAUSSIAN_BLUR -> PSGaussianBlurFilter()
                PSFilterType.SHARPEN -> PSSharpenFilter()
                PSFilterType.SATURATION -> PSSaturationFilter()
                PSFilterType.CONTRAST -> PSContrastFilter()
                PSFilterType.ZOOM_BLUR -> PSZoomBlurFilter()
                PSFilterType.VIGNETTE -> PSVignetteFilter()
            }
        }

        return@let map
    }