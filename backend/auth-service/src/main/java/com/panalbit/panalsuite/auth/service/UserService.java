package com.panalbit.panalsuite.auth.service;

import com.panalbit.panalsuite.auth.dto.LoginRequest;
import com.panalbit.panalsuite.auth.dto.LoginResponse;
import com.panalbit.panalsuite.auth.dto.UserDto;
import com.panalbit.panalsuite.auth.dto.UserRequest;
import com.panalbit.panalsuite.auth.exception.ApiException;
import com.panalbit.panalsuite.auth.model.Role;
import com.panalbit.panalsuite.auth.model.UserAccount;
import com.panalbit.panalsuite.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        String identifier = request.username().trim();
        UserAccount user = repository.findByUsername(identifier)
                .or(() -> findUniqueEmail(identifier))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        return new LoginResponse(tokenService.issue(user.getId()), UserDto.from(user));
    }

    private Optional<UserAccount> findUniqueEmail(String email) {
        var matches = repository.findAllByEmailIgnoreCase(email);
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }

    public UserDto currentUser(String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7) : null;
        long userId = tokenService.validate(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado"));
        return repository.findById(userId).map(UserDto::from)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida"));
    }

    public void requireAdministrator(String authorizationHeader) {
        if (currentUser(authorizationHeader).role() != Role.ADMINISTRADOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede gestionar usuarios");
        }
    }

    public List<UserDto> list() {
        return repository.findAll().stream().map(UserDto::from).toList();
    }

    public UserDto get(Long id) {
        return UserDto.from(find(id));
    }

    public UserDto create(UserRequest request) {
        if (request.password() == null || request.password().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }
        if (repository.existsByUsername(request.username().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
        }
        UserAccount user = new UserAccount(request.username().trim(), request.name().trim(),
                request.email().trim(), request.role(), passwordEncoder.encode(request.password()));
        return UserDto.from(repository.save(user));
    }

    public UserDto update(Long id, UserRequest request) {
        UserAccount user = find(id);
        repository.findByUsername(request.username().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
                });
        user.setUsername(request.username().trim());
        user.setName(request.name().trim());
        user.setEmail(request.email().trim());
        user.setRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 6) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
            }
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserDto.from(repository.save(user));
    }

    public void delete(Long id) {
        repository.delete(find(id));
    }

    private UserAccount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
