package com.panalbit.panalsuite.tickets.dto;

import com.panalbit.panalsuite.tickets.model.TicketComment;

import java.time.LocalDateTime;

public record CommentDto(Long id, Long ticketId, Long authorId, String authorName, String text,
                         boolean internal, LocalDateTime createdAt) {

    public static CommentDto from(TicketComment c) {
        return new CommentDto(c.getId(), c.getTicketId(), c.getAuthorId(), c.getAuthorName(), c.getText(),
                c.isInternal(), c.getCreatedAt());
    }
}
