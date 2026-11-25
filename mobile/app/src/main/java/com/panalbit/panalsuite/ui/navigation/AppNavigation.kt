package com.panalbit.panalsuite.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.panalbit.panalsuite.di.AppContainer
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.ui.AppViewModel
import com.panalbit.panalsuite.ui.SessionState
import com.panalbit.panalsuite.ui.home.HomeScreen
import com.panalbit.panalsuite.ui.login.LoginScreen
import com.panalbit.panalsuite.ui.login.LoginViewModel
import com.panalbit.panalsuite.ui.profile.ManageUsersScreen
import com.panalbit.panalsuite.ui.profile.ManageUsersViewModel
import com.panalbit.panalsuite.ui.rememberViewModel
import com.panalbit.panalsuite.ui.theme.PanalSuiteTheme
import com.panalbit.panalsuite.ui.tickets.CreateEditTicketScreen
import com.panalbit.panalsuite.ui.tickets.CreateEditTicketViewModel
import com.panalbit.panalsuite.ui.tickets.TicketDetailScreen
import com.panalbit.panalsuite.ui.tickets.TicketDetailViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val USERS = "users"
    const val DETAIL = "ticket/{ticketId}"
    const val FORM = "ticket_form?ticketId={ticketId}"

    fun detail(id: Long) = "ticket/$id"
    fun form(id: Long? = null) = "ticket_form?ticketId=${id ?: -1L}"
}

/** Raiz de la app: decide entre login y pantalla principal segun la sesion guardada en DataStore. */
@Composable
fun PanalSuiteRoot(container: AppContainer) {
    val appViewModel = rememberViewModel { AppViewModel(container.authRepository) }
    val sessionState by appViewModel.sessionState.collectAsStateWithLifecycle()

    PanalSuiteTheme {
        when (val session = sessionState) {
            SessionState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> AppNavHost(
                container = container,
                startLoggedIn = session is SessionState.LoggedIn,
                user = (session as? SessionState.LoggedIn)?.user,
                onLogout = appViewModel::logout
            )
        }
    }
}

@Composable
private fun AppNavHost(container: AppContainer, startLoggedIn: Boolean, user: User?, onLogout: () -> Unit) {
    val navController = rememberNavController()

    // Si la sesion se cierra, se vuelve al login y se limpia el historial
    LaunchedEffect(startLoggedIn, user) {
        if (user == null && navController.currentDestination?.route != Routes.LOGIN) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) Routes.HOME else Routes.LOGIN,
        enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)) }
    ) {
        composable(Routes.LOGIN) {
            val viewModel = rememberViewModel { LoginViewModel(container.authRepository) }
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Routes.HOME) {
            if (user != null) {
                HomeScreen(
                    container = container,
                    user = user,
                    onOpenTicket = { navController.navigate(Routes.detail(it)) },
                    onCreateTicket = {
                        navController.navigate(Routes.form()) { launchSingleTop = true }
                    },
                    onManageUsers = { navController.navigate(Routes.USERS) },
                    onLogout = onLogout
                )
            }
        }

        composable(Routes.USERS) {
            if (user?.role == Roles.ADMIN) {
                val viewModel = rememberViewModel { ManageUsersViewModel(container.authRepository) }
                ManageUsersScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
        ) { entry: NavBackStackEntry ->
            val ticketId = entry.arguments?.getLong("ticketId") ?: -1L
            if (user != null) {
                val viewModel = rememberViewModel(key = "detail-$ticketId") {
                    TicketDetailViewModel(container.ticketRepository, container.authRepository, ticketId, user)
                }
                TicketDetailScreen(
                    viewModel = viewModel,
                    user = user,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.form(it)) }
                )
            }
        }

        composable(
            route = Routes.FORM,
            arguments = listOf(navArgument("ticketId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry: NavBackStackEntry ->
            val ticketId = entry.arguments?.getLong("ticketId")?.takeIf { it != -1L }
            if (user != null) {
                val viewModel = rememberViewModel(key = "form-${ticketId ?: "new"}") {
                    CreateEditTicketViewModel(container.ticketRepository, user, ticketId)
                }
                CreateEditTicketScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
