package com.panalbit.panalsuite.testutil

import com.panalbit.panalsuite.data.local.SessionStorage
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.domain.Session
import com.panalbit.panalsuite.domain.Ticket
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Reemplaza Dispatchers.Main para poder probar ViewModels en la JVM. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeSessionStorage(initial: Session? = null) : SessionStorage {
    private val state = MutableStateFlow(initial)
    override val session: Flow<Session?> = state
    override suspend fun save(token: String, user: User) { state.value = Session(token, user) }
    override suspend fun clear() { state.value = null }
}

object Samples {
    val requester = User(1, "solicitante", "Ana Solicitante", "ana@panal.cl", Roles.REQUESTER)
    val resolver = User(2, "resolutor", "Raúl Resolutor", "raul@panal.cl", Roles.RESOLVER)
    val supervisor = User(3, "supervisor", "Sofía Supervisora", "sofia@panal.cl", Roles.SUPERVISOR)
    val admin = User(5, "admin", "Admin", "admin@panal.cl", Roles.ADMIN)

    fun ticket(
        id: Long = 1,
        title: String = "Impresora sin tinta",
        status: String = "NUEVO",
        requesterId: Long = 1,
        category: String = "TI"
    ) = Ticket(
        id = id, ticketNumber = "TKT-2026-%03d".format(id), title = title, description = "Descripción larga del problema",
        type = "INCIDENTE", category = category, priority = "MEDIA", status = status, branch = "Casa Matriz",
        requesterId = requesterId, requesterName = "Ana Solicitante", assignedToId = null, assignedToName = null,
        createdAt = "2026-11-17T12:30:45", updatedAt = "2026-11-17T12:30:45"
    )
}
