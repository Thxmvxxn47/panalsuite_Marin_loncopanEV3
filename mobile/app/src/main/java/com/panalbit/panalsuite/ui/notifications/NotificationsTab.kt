@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.ui.components.EmptyState
import com.panalbit.panalsuite.ui.components.ErrorMessage
import com.panalbit.panalsuite.util.Labels

@Composable
fun NotificationsTab(viewModel: NotificationsViewModel, userId: Long, onOpenTicket: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error = state.error

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        when {
            error != null && state.items.isEmpty() -> ErrorMessage(error, onRetry = { viewModel.load(userId) })
            state.items.isEmpty() && !state.isLoading -> EmptyState("No tienes notificaciones")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    Card(
                        onClick = {
                            if (!item.read) viewModel.markRead(item.id)
                            item.ticketId?.let(onOpenTicket)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.read) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(10.dp)
                                    .background(
                                        if (item.read) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(item.message, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    Labels.dateTime(item.createdAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
