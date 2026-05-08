package com.jongwoo_dev.awgdas_v2.dto;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;

import java.time.LocalDateTime;

public record UserListItem(
        Long id,
        String username,
        String email,
        Role role,
        boolean enabled,
        int quota,
        LocalDateTime createdAt
) {
    public static UserListItem from(User user) {
        return new UserListItem(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getQuota(),
                user.getCreatedAt()
        );
    }
}
