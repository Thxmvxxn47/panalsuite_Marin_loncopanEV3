package com.panalbit.panalsuite.auth.dto;

public record LoginResponse(String token, UserDto user) {
}
