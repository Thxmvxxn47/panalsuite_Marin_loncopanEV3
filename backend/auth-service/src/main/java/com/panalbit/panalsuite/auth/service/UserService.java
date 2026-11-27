package com.panalbit.panalsuite.auth.service;

import com.panalbit.panalsuite.auth.dto.LoginRequest;
import com.panalbit.panalsuite.auth.dto.LoginResponse;
import com.panalbit.panalsuite.auth.dto.UserDto;
import com.panalbit.panalsuite.auth.dto.UserRequest;
import com.panalbit.panalsuite.auth.exception.ApiException;
import com.panalbit.panalsuite.auth.model.UserAccount;
import com.panalbit.panalsuite.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        UserAccount user = repository.findByUsernameIgnoreCase(request.username().trim())
                .or(() -> { var matches = repository.findAllByEmailIgnoreCase(request.username().trim());
                    return matches.size() == 1 ? java.util.Optional.of(matches.get(0)) : java.util.Optional.empty(); })
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        return new LoginResponse(tokenService.issue(user.getId()), UserDto.from(user));
    }

    public UserDto currentUser(String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7) : null;
        long userId = tokenService.validate(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado"));
        return get(userId);
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
        if (repository.findByUsernameIgnoreCase(request.username().trim()).isPresent() || !repository.findAllByEmailIgnoreCase(request.email().trim()).isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
        }
        UserAccount user = new UserAccount(request.username().trim(), request.name().trim(),
                request.email().trim(), request.role(), passwordEncoder.encode(request.password()));
        return UserDto.from(repository.save(user));
    }

    public UserDto update(Long id, UserRequest request) {
        UserAccount user = find(id);
        repository.findByUsernameIgnoreCase(request.username().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
                });
        if (repository.findAllByEmailIgnoreCase(request.email().trim()).stream().anyMatch(other -> !other.getId().equals(id))) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya existe");
        }
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
