package com.panalbit.panalsuite.auth.controller;

import com.panalbit.panalsuite.auth.dto.LoginRequest;
import com.panalbit.panalsuite.auth.dto.LoginResponse;
import com.panalbit.panalsuite.auth.dto.UserDto;
import com.panalbit.panalsuite.auth.dto.UserRequest;
import com.panalbit.panalsuite.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @Valid @RequestBody UserRequest request) {
        service.requireAdministrator(authorization);
        return service.create(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.currentUser(authorization);
    }
}
