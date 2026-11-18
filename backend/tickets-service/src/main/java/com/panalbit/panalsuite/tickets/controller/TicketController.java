package com.panalbit.panalsuite.tickets.controller;

import com.panalbit.panalsuite.tickets.dto.*;
import com.panalbit.panalsuite.tickets.model.TicketPriority;
import com.panalbit.panalsuite.tickets.model.TicketStatus;
import com.panalbit.panalsuite.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<TicketDto> list(@RequestParam(required = false) TicketStatus status,
                                @RequestParam(required = false) TicketPriority priority,
                                @RequestParam(required = false) Long assignedToId,
                                @RequestParam(required = false) Long requesterId) {
        return service.list(status, priority, assignedToId, requesterId);
    }

    @GetMapping("/stats")
    public StatsDto stats(@RequestParam(required = false) Long assignedToId) {
        return service.stats(assignedToId);
    }

    @GetMapping("/{id}")
    public TicketDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDto create(@Valid @RequestBody CreateTicketRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TicketDto update(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TicketDto changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> comments(@PathVariable Long id) {
        return service.comments(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return service.addComment(id, request);
    }
}
