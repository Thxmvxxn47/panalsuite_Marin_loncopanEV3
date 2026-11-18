@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.domain.Stats
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.ui.components.ErrorMessage
import com.panalbit.panalsuite.ui.components.OfflineBanner
import com.panalbit.panalsuite.ui.components.SectionTitle
import com.panalbit.panalsuite.ui.components.TicketCard
import com.panalbit.panalsuite.ui.theme.AppColors
import com.panalbit.panalsuite.util.Labels

@Composable
fun DashboardTab(
    user: User,
    viewModel: DashboardViewModel,
    onOpenTicket: (Long) -> Unit,
    onSeeAll: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hola, ${user.name.substringBefore(' ')}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${Labels.role(user.role)} · ${user.email}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (state.isLoading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        if (state.offline) item { OfflineBanner() }
        state.error?.let { message -> item { ErrorMessage(message, onRetry = { viewModel.load(user) }) } }

        state.stats?.let { stats ->
            item { SectionTitle("Resumen") }
            val cards = statCards(stats, user.role)
            items(cards.chunked(2)) { rowCards ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowCards.forEach { card ->
                        StatCard(card.label, card.value, card.color, Modifier.weight(1f))
                    }
                    if (rowCards.size == 1) Row(modifier = Modifier.weight(1f)) {}
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Tickets recientes")
                TextButton(onClick = onSeeAll) { Text("Ver todos") }
            }
        }
        items(state.recentTickets, key = { it.id }) { ticket ->
            TicketCard(ticket = ticket, onClick = { onOpenTicket(ticket.id) })
        }
    }
}

private data class StatCardData(val label: String, val value: Long, val color: Color)

private fun statCards(stats: Stats, role: String): List<StatCardData> {
    val cards = mutableListOf(
        StatCardData("Nuevos", stats.open, AppColors.status("NUEVO")),
        StatCardData("En progreso", stats.inProgress, AppColors.status("EN_PROGRESO")),
        StatCardData("Pendientes", stats.pending, AppColors.status("PENDIENTE")),
        StatCardData("Resueltos", stats.resolved, AppColors.status("RESUELTO")),
        StatCardData("Críticos activos", stats.critical, AppColors.priority("CRITICA")),
        StatCardData("Altos activos", stats.high, AppColors.priority("ALTA"))
    )
    if (role == Roles.RESOLVER) {
        cards.add(0, StatCardData("Asignados a mí", stats.myAssigned, Color(0xFFB45309)))
    }
    return cards
}

@Composable
private fun StatCard(label: String, value: Long, color: Color, modifier: Modifier = Modifier) {
    // Contador animado: sube desde 0 hasta el valor real
    val animated by animateIntAsState(targetValue = value.toInt(), animationSpec = tween(800), label = "stat")
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(animated.toString(), style = MaterialTheme.typography.headlineSmall, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
