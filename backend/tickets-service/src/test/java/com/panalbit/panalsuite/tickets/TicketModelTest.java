package com.panalbit.panalsuite.tickets;

import com.panalbit.panalsuite.tickets.model.Ticket;
import com.panalbit.panalsuite.tickets.model.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketModelTest {

    @Test
    void statusEnumHasExpectedWorkflowValues() {
        assertEquals(5, TicketStatus.values().length);
        assertEquals(TicketStatus.NUEVO, TicketStatus.valueOf("NUEVO"));
    }

    @Test
    void ticketKeepsAssignedUser() {
        Ticket ticket = new Ticket();
        ticket.setAssignedToId(2L);
        ticket.setAssignedToName("Carlos");
        assertEquals(2L, ticket.getAssignedToId());
        assertEquals("Carlos", ticket.getAssignedToName());
    }
}
