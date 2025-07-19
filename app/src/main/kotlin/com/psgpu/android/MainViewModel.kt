package com.psgpu.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.psgpu.android.filter.PSFilterException
import com.psgpu.android.filter.PSFilterType
import com.psgpu.android.filter.PSGaussianBlurFilter
import com.psgpu.android.filter.PSNoFilter
import com.psgpu.android.filter.template.PSTemplateFilter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
                val filterType = event.type
                when (filterType) {
                    PSFilterType.NO_FILTER -> {
                        val filter = PSTemplateFilter()
                        val filteredBitmap = try {
                            filter.apply(defaultBitmap)
                        } catch (e: PSFilterException) {
                            Log.d("@@@", "PSNoFilter Error: $e")
                            return
                        }
                        _state.update {
                            it.copy(
                                bitmap = filteredBitmap,
                                selectedFilter = filterType
                            )
                        }
                    }
                    PSFilterType.GAUSSIAN_BLUR -> {
                        val filter = PSGaussianBlurFilter()
                        val filteredBitmap = filter.apply(defaultBitmap)
                        _state.update {
                            it.copy(
                                bitmap = filteredBitmap,
                                selectedFilter = filterType
                            )
                        }
                    }
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
                filters = PSFilterType.entries
            )
        }
    }
}