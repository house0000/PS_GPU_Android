package com.psgpu.android.ui.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.psgpu.android.filter.PSFilter
import com.psgpu.android.filter.PSFilterType

data class FilterItemState(
    val filter: PSFilterType,
    val onSelect: () -> Unit,
    val sliders: List<SliderState> = emptyList()
) {
    data class SliderState(
        val title: String,
        val min: Float,
        val max: Float,
        val onSlide: (Float) -> Unit
    )
}

@Composable
internal fun ColumnScope.FilterItem(
    state: FilterItemState,
    selected: Boolean
) {
    TitleAndSelection(state.filter.title, selected, state.onSelect)

    if (selected) {
        if (state.sliders.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                state.sliders.forEach { slider ->
                    SliderCell(slider)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.TitleAndSelection(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
            }
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = {
                onSelect()
            }
        )

        Text(title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.SliderCell(
    state: FilterItemState.SliderState
) {
    var sliderValue by remember { mutableFloatStateOf(state.min) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(state.title + ": ")
        Text("${state.min} ~ ${state.max} ")

        Slider(
            modifier = Modifier.weight(1f),
            value = sliderValue,
            valueRange = state.min..state.max,
            onValueChange = {
                sliderValue = it
                state.onSlide(it)
            }
        )

        Text(String.format("%.1f", sliderValue))
    }
}