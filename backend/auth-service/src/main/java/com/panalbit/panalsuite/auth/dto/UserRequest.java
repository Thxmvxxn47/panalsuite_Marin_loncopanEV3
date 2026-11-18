package com.panalbit.panalsuite.auth.dto;

import com.panalbit.panalsuite.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo para crear usuarios (registro). En una actualizacion la contraseña puede venir vacia. */
public record UserRequest(
        @NotBlank(message = "El usuario es obligatorio") @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres") String username,
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String email,
        @NotNull(message = "El rol es obligatorio") Role role,
        String password) {
}
