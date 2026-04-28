package com.jongwoo_dev.awgdas_v2.repository;

import com.jongwoo_dev.awgdas_v2.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
