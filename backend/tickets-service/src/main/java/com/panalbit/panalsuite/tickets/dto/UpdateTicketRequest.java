package com.panalbit.panalsuite.tickets.dto;

import com.panalbit.panalsuite.tickets.model.TicketCategory;
import com.panalbit.panalsuite.tickets.model.TicketPriority;
import com.panalbit.panalsuite.tickets.model.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 150, message = "El título admite hasta 150 caracteres") String title,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres") String description,
        @NotNull(message = "El tipo es obligatorio") TicketType type,
        @NotNull(message = "La categoría es obligatoria") TicketCategory category,
        @NotNull(message = "La prioridad es obligatoria") TicketPriority priority,
        String branch) {
}
