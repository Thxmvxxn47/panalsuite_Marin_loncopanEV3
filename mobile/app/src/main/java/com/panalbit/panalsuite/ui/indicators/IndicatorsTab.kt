@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.indicators

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.ui.components.ErrorMessage
import java.util.Locale

/** Muestra datos de la API EXTERNA (mindicador.cl), separados de los tickets propios. */
@Composable
fun IndicatorsTab(viewModel: IndicatorsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error = state.error

    LaunchedEffect(Unit) { if (viewModel.state.value.items.isEmpty()) viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (error != null && state.items.isEmpty()) {
            ErrorMessage(error, onRetry = { viewModel.load() })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Indicadores económicos de Chile · fuente externa: mindicador.cl",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.items, key = { it.code }) { indicator ->
                    AnimatedVisibility(visible = true) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(indicator.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${indicator.unit} · ${formatDate(indicator.date)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatValue(indicator.value),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun formatValue(value: Double): String = String.format(Locale("es", "CL"), "%,.2f", value)

internal fun formatDate(iso: String): String {
    val date = iso.take(10).split('-')
    return if (date.size == 3) "${date[2]}-${date[1]}-${date[0]}" else iso
}
