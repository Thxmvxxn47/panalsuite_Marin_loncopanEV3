package com.panalbit.panalsuite.tickets.dto;

import com.panalbit.panalsuite.tickets.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cambio de estado y, opcionalmente, (re)asignacion del ticket. */
public record StatusChangeRequest(
        @NotNull(message = "El nuevo estado es obligatorio") TicketStatus status,
        Long assignedToId,
        String assignedToName,
        @NotNull(message = "El usuario que ejecuta la acción es obligatorio") Long actorId,
        @NotBlank(message = "El nombre del usuario es obligatorio") String actorName) {
}
