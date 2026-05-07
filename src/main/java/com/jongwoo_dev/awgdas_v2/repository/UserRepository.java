package com.jongwoo_dev.awgdas_v2.repository;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Page<User> findByEnabled(boolean enabled, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndEnabled(Role role, boolean enabled, Pageable pageable);

    long countByRoleAndEnabled(Role role, boolean enabled);
}
