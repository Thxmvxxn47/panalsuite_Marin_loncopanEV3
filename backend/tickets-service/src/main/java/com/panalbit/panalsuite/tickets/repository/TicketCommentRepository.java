package com.panalbit.panalsuite.tickets.repository;

import com.panalbit.panalsuite.tickets.model.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    void deleteByTicketId(Long ticketId);
}
