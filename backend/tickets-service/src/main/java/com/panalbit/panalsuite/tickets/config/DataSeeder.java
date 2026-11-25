package com.panalbit.panalsuite.tickets.config;

import com.panalbit.panalsuite.tickets.model.*;
import com.panalbit.panalsuite.tickets.repository.NotificationRepository;
import com.panalbit.panalsuite.tickets.repository.TicketCommentRepository;
import com.panalbit.panalsuite.tickets.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Datos de demostracion. Los ids de usuario coinciden con los que crea auth-service (1..5). */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final long REQUESTER_MIZAHEL = 1L;
    private static final long RESOLVER_CARLOS = 2L;

    private final TicketRepository tickets;
    private final TicketCommentRepository comments;
    private final NotificationRepository notifications;

    public DataSeeder(TicketRepository tickets, TicketCommentRepository comments,
                      NotificationRepository notifications) {
        this.tickets = tickets;
        this.comments = comments;
        this.notifications = notifications;
    }

    @Override
    public void run(String... args) {
        if (tickets.count() > 0) {
            return;
        }
        Ticket first = seed("Falla en sistema POS Tienda Alameda",
                "El sistema POS presenta fallas intermitentes al procesar pagos con tarjeta débito. Unas 15 transacciones fallidas desde las 9:00 hrs.",
                TicketType.INCIDENTE, TicketCategory.TI, TicketPriority.CRITICA, TicketStatus.EN_PROGRESO,
                "Tienda Alameda", REQUESTER_MIZAHEL, "Mizahel", RESOLVER_CARLOS, 5);
        seed("Solicitud acceso sistema de cobranzas",
                "Se requiere acceso de lectura y reportes al módulo de cobranzas para el nuevo analista.",
                TicketType.REQUERIMIENTO, TicketCategory.COBRANZA, TicketPriority.MEDIA, TicketStatus.NUEVO,
                "Casa Matriz", REQUESTER_MIZAHEL, "Mizahel", null, 4);
        seed("Error en impresión de boletas farmacia sur",
                "La impresora de la caja 2 imprime caracteres ilegibles y a veces se atasca el papel.",
                TicketType.INCIDENTE, TicketCategory.INFRAESTRUCTURA, TicketPriority.ALTA, TicketStatus.PENDIENTE,
                "Farmacia Sur", 6L, "Pedro González", RESOLVER_CARLOS, 26);
        seed("Gestión de gastos Q1 - revisión proveedores",
                "Revisar facturas pendientes del trimestre: validar montos, vencimientos y estado de pago.",
                TicketType.CONSULTA, TicketCategory.GASTOS, TicketPriority.BAJA, TicketStatus.EN_PROGRESO,
                "Proveedor ABC", 7L, "Lucía Fernández", RESOLVER_CARLOS, 48);
        seed("Red sin conexión sucursal Providencia",
                "Pérdida total de conectividad desde las 16:30 hrs. El router principal muestra luz roja.",
                TicketType.INCIDENTE, TicketCategory.INFRAESTRUCTURA, TicketPriority.CRITICA, TicketStatus.RESUELTO,
                "Sucursal Providencia", 8L, "Manuel Rojas", RESOLVER_CARLOS, 70);
        seed("Capacitación nuevo sistema legal",
                "El departamento legal requiere capacitación para 5 personas en el módulo de gestión documental.",
                TicketType.REQUERIMIENTO, TicketCategory.TI, TicketPriority.BAJA, TicketStatus.NUEVO,
                "Departamento Legal", 9L, "Daniela Soto", null, 3);
        seed("Falla terminal pago contactless",
                "El terminal no lee tarjetas NFC; las tarjetas convencionales funcionan con normalidad.",
                TicketType.INCIDENTE, TicketCategory.TI, TicketPriority.ALTA, TicketStatus.EN_PROGRESO,
                "Tienda Plaza Norte", 10L, "Javier Moreno", RESOLVER_CARLOS, 8);
        seed("Reporte mensual cobranzas",
                "Generar el reporte mensual de cobranzas del mes anterior.",
                TicketType.REQUERIMIENTO, TicketCategory.COBRANZA, TicketPriority.MEDIA, TicketStatus.CERRADO,
                "Casa Matriz", 11L, "Carolina Muñoz", RESOLVER_CARLOS, 120);

        comment(first, 3L, "Darliette", "Se asigna al equipo de soporte nivel 2 para diagnóstico.", false);
        comment(first, RESOLVER_CARLOS, "Carlos", "Diagnóstico inicial listo, trabajando en la solución.", false);

        Notification notification = new Notification();
        notification.setRecipientId(RESOLVER_CARLOS);
        notification.setTitle("Ticket asignado");
        notification.setMessage("Se te asignó el ticket " + first.getTicketNumber() + ": " + first.getTitle());
        notification.setType(TicketNotificationType.ASIGNACION);
        notification.setTicketId(first.getId());
        notification.setTicketNumber(first.getTicketNumber());
        notification.setCreatedAt(LocalDateTime.now().minusHours(4));
        notifications.save(notification);
    }

    private Ticket seed(String title, String description, TicketType type, TicketCategory category,
                        TicketPriority priority, TicketStatus status, String branch,
                        Long requesterId, String requesterName, Long assignedToId, int hoursAgo) {
        LocalDateTime created = LocalDateTime.now().minusHours(hoursAgo);
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setType(type);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setBranch(branch);
        ticket.setRequesterId(requesterId);
        ticket.setRequesterName(requesterName);
        ticket.setAssignedToId(assignedToId);
        ticket.setAssignedToName(assignedToId == null ? null : "Carlos");
        ticket.setCreatedAt(created);
        ticket.setUpdatedAt(created.plusHours(Math.min(hoursAgo, 2)));
        Ticket saved = tickets.save(ticket);
        saved.setTicketNumber(String.format("TKT-%d-%03d", created.getYear(), saved.getId()));
        return tickets.save(saved);
    }

    private void comment(Ticket ticket, Long authorId, String authorName, String text, boolean internal) {
        TicketComment comment = new TicketComment();
        comment.setTicketId(ticket.getId());
        comment.setAuthorId(authorId);
        comment.setAuthorName(authorName);
        comment.setText(text);
        comment.setInternal(internal);
        comment.setCreatedAt(LocalDateTime.now().minusHours(1));
        comments.save(comment);
    }
}
