package com.panalbit.panalsuite.tickets.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 120)
    private String authorName;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "is_internal", nullable = false)
    private boolean internal;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
