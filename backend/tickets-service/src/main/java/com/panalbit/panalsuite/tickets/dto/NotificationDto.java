package com.panalbit.panalsuite.tickets.dto;

import com.panalbit.panalsuite.tickets.model.Notification;
import com.panalbit.panalsuite.tickets.model.TicketNotificationType;

import java.time.LocalDateTime;

public record NotificationDto(Long id, Long recipientId, String title, String message, TicketNotificationType type,
                              Long ticketId, String ticketNumber, boolean read, LocalDateTime createdAt) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getRecipientId(), n.getTitle(), n.getMessage(), n.getType(),
                n.getTicketId(), n.getTicketNumber(), n.isRead(), n.getCreatedAt());
    }
}
