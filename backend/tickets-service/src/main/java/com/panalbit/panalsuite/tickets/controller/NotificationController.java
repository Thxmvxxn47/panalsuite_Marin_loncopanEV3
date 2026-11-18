package com.panalbit.panalsuite.tickets.controller;

import com.panalbit.panalsuite.tickets.dto.NotificationDto;
import com.panalbit.panalsuite.tickets.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final TicketService service;

    public NotificationController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationDto> list(@RequestParam Long userId) {
        return service.notifications(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable Long id) {
        return service.markRead(id);
    }
}
