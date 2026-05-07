package com.jongwoo_dev.awgdas_v2.dto;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotNull
        Role role
) {
}
