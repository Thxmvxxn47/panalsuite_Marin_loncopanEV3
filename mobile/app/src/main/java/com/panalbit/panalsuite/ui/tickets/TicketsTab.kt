@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.tickets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.domain.Status
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.ui.components.EmptyState
import com.panalbit.panalsuite.ui.components.ErrorMessage
import com.panalbit.panalsuite.ui.components.OfflineBanner
import com.panalbit.panalsuite.ui.components.TicketCard
import com.panalbit.panalsuite.util.Labels

@Composable
fun TicketsTab(viewModel: TicketsViewModel, user: User, onOpenTicket: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visible = state.visibleTickets

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar por título, número o categoría") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (listOf(ALL_STATUSES) + Status.all).forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status,
                    onClick = { viewModel.onStatusFilter(status) },
                    label = { Text(if (status == ALL_STATUSES) "Todos" else Labels.status(status)) }
                )
            }
        }

        if (state.isLoading) LinearProgressIndicator(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp))
        if (state.offline) OfflineBanner(modifier = Modifier.padding(16.dp))

        val error = state.error
        when {
            error != null && state.allTickets.isEmpty() -> ErrorMessage(error, onRetry = { viewModel.load(user) })
            visible.isEmpty() && !state.isLoading -> EmptyState("No hay tickets que coincidan con tu búsqueda")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { ticket ->
                    TicketCard(ticket = ticket, onClick = { onOpenTicket(ticket.id) })
                }
            }
        }
    }
}
