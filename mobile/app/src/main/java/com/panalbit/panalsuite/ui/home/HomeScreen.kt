@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.home

import androidx.compose.foundation.layout.padding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.di.AppContainer
import com.panalbit.panalsuite.domain.TicketRules
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.ui.indicators.IndicatorsTab
import com.panalbit.panalsuite.ui.indicators.IndicatorsViewModel
import com.panalbit.panalsuite.ui.notifications.NotificationsTab
import com.panalbit.panalsuite.ui.notifications.NotificationsViewModel
import com.panalbit.panalsuite.ui.profile.ProfileTab
import com.panalbit.panalsuite.ui.rememberViewModel
import com.panalbit.panalsuite.ui.tickets.TicketsTab
import com.panalbit.panalsuite.ui.tickets.TicketsViewModel

private enum class HomeTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Home),
    TICKETS("Tickets", Icons.Default.List),
    NOTIFICATIONS("Avisos", Icons.Default.Notifications),
    INDICATORS("Indicadores", Icons.Default.Info),
    PROFILE("Perfil", Icons.Default.Person)
}

@Composable
fun HomeScreen(
    container: AppContainer,
    user: User,
    onOpenTicket: (Long) -> Unit,
    onCreateTicket: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardVm = rememberViewModel(key = "dashboard-${user.id}") { DashboardViewModel(container.ticketRepository) }
    val ticketsVm = rememberViewModel(key = "tickets-${user.id}") { TicketsViewModel(container.ticketRepository) }
    val notificationsVm = rememberViewModel(key = "notifications-${user.id}") { NotificationsViewModel(container.ticketRepository) }
    val indicatorsVm = rememberViewModel(key = "indicators") { IndicatorsViewModel(container.indicatorRepository) }

    val notifications by notificationsVm.state.collectAsStateWithLifecycle()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val tab = HomeTab.entries[selectedIndex]

    // Al abrir y al volver de otra pantalla se refrescan los datos desde los microservicios
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        dashboardVm.load(user)
        ticketsVm.load(user)
        notificationsVm.load(user.id)
    }

    fun refresh() {
        when (tab) {
            HomeTab.DASHBOARD -> dashboardVm.load(user)
            HomeTab.TICKETS -> ticketsVm.load(user)
            HomeTab.NOTIFICATIONS -> notificationsVm.load(user.id)
            HomeTab.INDICATORS -> indicatorsVm.load()
            HomeTab.PROFILE -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tab.title) },
                actions = {
                    if (tab != HomeTab.PROFILE) {
                        IconButton(onClick = { refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        icon = {
                            if (item == HomeTab.NOTIFICATIONS && notifications.unreadCount > 0) {
                                BadgedBox(badge = { Badge { Text(notifications.unreadCount.toString()) } }) {
                                    Icon(item.icon, contentDescription = item.title)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.title)
                            }
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = (tab == HomeTab.TICKETS || tab == HomeTab.DASHBOARD) && TicketRules.canCreate(user.role),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCreateTicket,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva solicitud") }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.padding(padding),
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "home-tab"
        ) { current ->
            when (current) {
                HomeTab.DASHBOARD -> DashboardTab(
                    user = user,
                    viewModel = dashboardVm,
                    onOpenTicket = onOpenTicket,
                    onSeeAll = { selectedIndex = HomeTab.TICKETS.ordinal }
                )
                HomeTab.TICKETS -> TicketsTab(viewModel = ticketsVm, user = user, onOpenTicket = onOpenTicket)
                HomeTab.NOTIFICATIONS -> NotificationsTab(
                    viewModel = notificationsVm,
                    userId = user.id,
                    onOpenTicket = onOpenTicket
                )
                HomeTab.INDICATORS -> IndicatorsTab(viewModel = indicatorsVm)
                HomeTab.PROFILE -> ProfileTab(user = user, onLogout = onLogout)
            }
        }
    }
}
