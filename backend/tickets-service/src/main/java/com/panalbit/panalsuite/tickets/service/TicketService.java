package com.panalbit.panalsuite.tickets.service;

import com.panalbit.panalsuite.tickets.dto.*;
import com.panalbit.panalsuite.tickets.exception.ApiException;
import com.panalbit.panalsuite.tickets.model.*;
import com.panalbit.panalsuite.tickets.repository.NotificationRepository;
import com.panalbit.panalsuite.tickets.repository.TicketCommentRepository;
import com.panalbit.panalsuite.tickets.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TicketService {

    private static final List<TicketStatus> FINISHED = List.of(TicketStatus.RESUELTO, TicketStatus.CERRADO);

    private final TicketRepository tickets;
    private final TicketCommentRepository comments;
    private final NotificationRepository notifications;

    public TicketService(TicketRepository tickets, TicketCommentRepository comments,
                         NotificationRepository notifications) {
        this.tickets = tickets;
        this.comments = comments;
        this.notifications = notifications;
    }

    // ---------------------------------------------------------------- tickets

    @Transactional(readOnly = true)
    public List<TicketDto> list(TicketStatus status, TicketPriority priority, Long assignedToId, Long requesterId) {
        return tickets.search(status, priority, assignedToId, requesterId).stream().map(TicketDto::from).toList();
    }

    @Transactional(readOnly = true)
    public TicketDto get(Long id) {
        return TicketDto.from(find(id));
    }

    public TicketDto create(CreateTicketRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setType(request.type());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setBranch(blankToNull(request.branch()));
        ticket.setRequesterId(request.requesterId());
        ticket.setRequesterName(request.requesterName().trim());
        ticket.setStatus(TicketStatus.NUEVO);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        Ticket saved = tickets.save(ticket);
        saved.setTicketNumber(buildNumber(saved));
        saved = tickets.save(saved);
        notify(saved.getRequesterId(), "Solicitud creada",
                "Tu solicitud " + saved.getTicketNumber() + " fue registrada correctamente",
                TicketNotificationType.CAMBIO_ESTADO, saved);
        return TicketDto.from(saved);
    }

    public TicketDto update(Long id, UpdateTicketRequest request) {
        Ticket ticket = find(id);
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setType(request.type());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setBranch(blankToNull(request.branch()));
        ticket.setUpdatedAt(LocalDateTime.now());
        return TicketDto.from(tickets.save(ticket));
    }

    public TicketDto changeStatus(Long id, StatusChangeRequest request) {
        Ticket ticket = find(id);
        TicketStatus previous = ticket.getStatus();
        Long previousAssignee = ticket.getAssignedToId();

        ticket.setStatus(request.status());
        if (request.assignedToId() != null) {
            ticket.setAssignedToId(request.assignedToId());
            ticket.setAssignedToName(blankToNull(request.assignedToName()));
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket saved = tickets.save(ticket);

        if (previous != saved.getStatus()) {
            String text = "El ticket " + saved.getTicketNumber() + " cambió de " + previous + " a " + saved.getStatus();
            if (!Objects.equals(saved.getRequesterId(), request.actorId())) {
                notify(saved.getRequesterId(), "Cambio de estado", text, TicketNotificationType.CAMBIO_ESTADO, saved);
            }
            if (saved.getAssignedToId() != null && !Objects.equals(saved.getAssignedToId(), request.actorId())) {
                notify(saved.getAssignedToId(), "Cambio de estado", text, TicketNotificationType.CAMBIO_ESTADO, saved);
            }
        }
        if (saved.getAssignedToId() != null && !Objects.equals(saved.getAssignedToId(), previousAssignee)) {
            notify(saved.getAssignedToId(), "Ticket asignado",
                    "Se te asignó el ticket " + saved.getTicketNumber() + ": " + saved.getTitle(),
                    TicketNotificationType.ASIGNACION, saved);
        }
        return TicketDto.from(saved);
    }

    public void delete(Long id) {
        Ticket ticket = find(id);
        comments.deleteByTicketId(id);
        notifications.deleteByTicketId(id);
        tickets.delete(ticket);
    }

    @Transactional(readOnly = true)
    public StatsDto stats(Long assignedToId) {
        long critical = tickets.countByPriorityAndStatusNotIn(TicketPriority.CRITICA, FINISHED);
        long high = tickets.countByPriorityAndStatusNotIn(TicketPriority.ALTA, FINISHED);
        long mine = assignedToId == null ? 0 : tickets.countByAssignedToIdAndStatusNotIn(assignedToId, FINISHED);
        return new StatsDto(tickets.count(),
                tickets.countByStatus(TicketStatus.NUEVO),
                tickets.countByStatus(TicketStatus.EN_PROGRESO),
                tickets.countByStatus(TicketStatus.PENDIENTE),
                tickets.countByStatus(TicketStatus.RESUELTO),
                tickets.countByStatus(TicketStatus.CERRADO),
                critical, high, mine);
    }

    // --------------------------------------------------------------- comments

    @Transactional(readOnly = true)
    public List<CommentDto> comments(Long ticketId) {
        find(ticketId);
        return comments.findByTicketIdOrderByCreatedAtAsc(ticketId).stream().map(CommentDto::from).toList();
    }

    public CommentDto addComment(Long ticketId, CommentRequest request) {
        Ticket ticket = find(ticketId);
        TicketComment comment = new TicketComment();
        comment.setTicketId(ticketId);
        comment.setAuthorId(request.authorId());
        comment.setAuthorName(request.authorName().trim());
        comment.setText(request.text().trim());
        comment.setInternal(request.internal());
        comment.setCreatedAt(LocalDateTime.now());
        TicketComment saved = comments.save(comment);

        ticket.setUpdatedAt(LocalDateTime.now());
        tickets.save(ticket);

        String text = request.authorName().trim() + " comentó en " + ticket.getTicketNumber();
        if (!request.internal() && !Objects.equals(ticket.getRequesterId(), request.authorId())) {
            notify(ticket.getRequesterId(), "Nuevo comentario", text, TicketNotificationType.COMENTARIO, ticket);
        }
        if (ticket.getAssignedToId() != null && !Objects.equals(ticket.getAssignedToId(), request.authorId())) {
            notify(ticket.getAssignedToId(), "Nuevo comentario", text, TicketNotificationType.COMENTARIO, ticket);
        }
        return CommentDto.from(saved);
    }

    // ---------------------------------------------------------- notifications

    @Transactional(readOnly = true)
    public List<NotificationDto> notifications(Long recipientId) {
        return notifications.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(NotificationDto::from).toList();
    }

    public NotificationDto markRead(Long notificationId) {
        Notification notification = notifications.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
        notification.setRead(true);
        return NotificationDto.from(notifications.save(notification));
    }

    // ---------------------------------------------------------------- helpers

    private Ticket find(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));
    }

    private void notify(Long recipientId, String title, String message, TicketNotificationType type, Ticket ticket) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTicketId(ticket.getId());
        notification.setTicketNumber(ticket.getTicketNumber());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notifications.save(notification);
    }

    static String buildNumber(Ticket ticket) {
        return String.format("TKT-%d-%03d", ticket.getCreatedAt().getYear(), ticket.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
