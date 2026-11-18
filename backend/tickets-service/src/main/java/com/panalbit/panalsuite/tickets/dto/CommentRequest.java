package com.panalbit.panalsuite.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotNull(message = "El autor es obligatorio") Long authorId,
        @NotBlank(message = "El nombre del autor es obligatorio") String authorName,
        @NotBlank(message = "El comentario no puede estar vacío") @Size(max = 1000, message = "El comentario admite hasta 1000 caracteres") String text,
        boolean internal) {
}
