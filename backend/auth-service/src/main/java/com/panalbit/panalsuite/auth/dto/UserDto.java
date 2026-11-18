package com.panalbit.panalsuite.auth.dto;

import com.panalbit.panalsuite.auth.model.Role;
import com.panalbit.panalsuite.auth.model.UserAccount;

public record UserDto(Long id, String username, String name, String email, Role role) {

    public static UserDto from(UserAccount user) {
        return new UserDto(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole());
    }
}
