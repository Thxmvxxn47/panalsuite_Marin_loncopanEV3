package com.panalbit.panalsuite.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketRulesTest {
    @Test fun resolver_actions_by_status() {
        assertEquals(listOf("TOMAR", "RECHAZAR"), TicketRules.availableActions(Status.NEW, Roles.RESOLVER))
        assertEquals(listOf("PAUSAR", "RESOLVER", "ESCALAR"), TicketRules.availableActions(Status.IN_PROGRESS, Roles.RESOLVER))
        assertEquals(listOf("REANUDAR", "RESOLVER"), TicketRules.availableActions(Status.PENDING, Roles.RESOLVER))
        assertTrue(TicketRules.availableActions(Status.CLOSED, Roles.RESOLVER).isEmpty())
    }
    @Test fun supervisor_actions_by_status() {
        assertTrue("ASIGNAR" in TicketRules.availableActions(Status.NEW, Roles.SUPERVISOR))
        assertTrue("ESCALAR" in TicketRules.availableActions(Status.IN_PROGRESS, Roles.SUPERVISOR))
        assertTrue("REANUDAR" in TicketRules.availableActions(Status.PENDING, Roles.SUPERVISOR))
        assertEquals(listOf("CERRAR", "REABRIR"), TicketRules.availableActions(Status.RESOLVED, Roles.SUPERVISOR))
        assertTrue(TicketRules.availableActions(Status.CLOSED, Roles.SUPERVISOR).isEmpty())
    }
    @Test fun admin_and_others() {
        assertEquals(5, TicketRules.availableActions(Status.NEW, Roles.ADMIN).size)
        assertTrue(TicketRules.availableActions(Status.NEW, Roles.REQUESTER).isEmpty())
        assertTrue(TicketRules.availableActions(Status.NEW, Roles.MANAGEMENT).isEmpty())
    }
    @Test fun status_mapping() {
        assertEquals(Status.IN_PROGRESS, TicketRules.statusFor("TOMAR"))
        assertEquals(Status.RESOLVED, TicketRules.statusFor("RESOLVER"))
        assertEquals(Status.CLOSED, TicketRules.statusFor("CERRAR"))
        assertNull(TicketRules.statusFor("INEXISTENTE"))
    }
    @Test fun assignee_required() {
        assertTrue(TicketRules.requiresAssignee("ASIGNAR"))
        assertTrue(TicketRules.requiresAssignee("REASIGNAR"))
        assertFalse(TicketRules.requiresAssignee("TOMAR"))
    }
    @Test fun permissions() {
        assertTrue(TicketRules.canCreate(Roles.REQUESTER))
        assertFalse(TicketRules.canCreate(Roles.MANAGEMENT))
        assertTrue(TicketRules.canEdit(Roles.SUPERVISOR))
        assertTrue(TicketRules.canEdit(Roles.ADMIN))
        assertFalse(TicketRules.canEdit(Roles.RESOLVER))
        assertTrue(TicketRules.canDelete(Roles.ADMIN))
        assertFalse(TicketRules.canDelete(Roles.SUPERVISOR))
        assertTrue(TicketRules.canComment(Roles.RESOLVER))
        assertFalse(TicketRules.canComment(Roles.MANAGEMENT))
        assertTrue(TicketRules.canWriteInternalComments(Roles.RESOLVER))
        assertFalse(TicketRules.canWriteInternalComments(Roles.REQUESTER))
        assertFalse(TicketRules.canWriteInternalComments(Roles.MANAGEMENT))
        assertTrue(TicketRules.canAddEvidence(Roles.REQUESTER))
        assertFalse(TicketRules.canAddEvidence(Roles.MANAGEMENT))
    }
}
