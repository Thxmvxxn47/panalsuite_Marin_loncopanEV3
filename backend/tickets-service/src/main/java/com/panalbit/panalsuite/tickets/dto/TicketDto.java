package com.panalbit.panalsuite.tickets.dto;

import com.panalbit.panalsuite.tickets.model.*;

import java.time.LocalDateTime;

public record TicketDto(
        Long id,
        String ticketNumber,
        String title,
        String description,
        TicketType type,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        String branch,
        Long requesterId,
        String requesterName,
        Long assignedToId,
        String assignedToName,
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) LocalDateTime createdAt,
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) LocalDateTime updatedAt) {

    public static TicketDto from(Ticket t) {
        return new TicketDto(t.getId(), t.getTicketNumber(), t.getTitle(), t.getDescription(), t.getType(),
                t.getCategory(), t.getPriority(), t.getStatus(), t.getBranch(), t.getRequesterId(),
                t.getRequesterName(), t.getAssignedToId(), t.getAssignedToName(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
