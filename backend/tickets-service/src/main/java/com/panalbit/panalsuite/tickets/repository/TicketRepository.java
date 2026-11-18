package com.panalbit.panalsuite.tickets.repository;

import com.panalbit.panalsuite.tickets.model.Ticket;
import com.panalbit.panalsuite.tickets.model.TicketPriority;
import com.panalbit.panalsuite.tickets.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            select t from Ticket t
            where (:status is null or t.status = :status)
              and (:priority is null or t.priority = :priority)
              and (:assignedToId is null or t.assignedToId = :assignedToId)
              and (:requesterId is null or t.requesterId = :requesterId)
            order by t.updatedAt desc
            """)
    List<Ticket> search(@Param("status") TicketStatus status,
                        @Param("priority") TicketPriority priority,
                        @Param("assignedToId") Long assignedToId,
                        @Param("requesterId") Long requesterId);

    long countByStatus(TicketStatus status);

    long countByPriorityAndStatusNotIn(TicketPriority priority, List<TicketStatus> statuses);

    long countByAssignedToIdAndStatusNotIn(Long assignedToId, List<TicketStatus> statuses);
}
