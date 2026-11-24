package com.panalbit.panalsuite.auth.controller;

import com.panalbit.panalsuite.auth.dto.UserDto;
import com.panalbit.panalsuite.auth.dto.UserRequest;
import com.panalbit.panalsuite.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestHeader(value = "Authorization", required = false) String authorization,
                          @Valid @RequestBody UserRequest request) {
        service.requireAdministrator(authorization);
        return service.create(request);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id,
                          @RequestHeader(value = "Authorization", required = false) String authorization,
                          @Valid @RequestBody UserRequest request) {
        service.requireAdministrator(authorization);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        service.requireAdministrator(authorization);
        service.delete(id);
    }
}
