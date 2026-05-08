package com.jongwoo_dev.awgdas_v2.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("builder — quota 미지정 시 기본값 10")
    void builder_defaultsQuotaToTen() {
        User user = User.builder()
                .username("alice")
                .passwordHash("$2a$10$x")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        assertThat(user.getQuota()).isEqualTo(10);
    }

    @Test
    @DisplayName("builder — quota 명시 시 그 값 사용")
    void builder_usesProvidedQuota() {
        User user = User.builder()
                .username("alice")
                .passwordHash("$2a$10$x")
                .email("alice@example.com")
                .role(Role.USER)
                .quota(25)
                .build();

        assertThat(user.getQuota()).isEqualTo(25);
    }

    @Test
    @DisplayName("adjustQuota — 양수 delta 정상 증가")
    void adjustQuota_positiveDeltaIncreases() {
        User user = User.builder()
                .username("alice").passwordHash("$2a$10$x").email("a@x").role(Role.USER).build();

        user.adjustQuota(1);
        assertThat(user.getQuota()).isEqualTo(11);

        user.adjustQuota(5);
        assertThat(user.getQuota()).isEqualTo(16);
    }

    @Test
    @DisplayName("adjustQuota — 0 delta는 IllegalArgumentException")
    void adjustQuota_zeroDeltaThrows() {
        User user = User.builder()
                .username("alice").passwordHash("$2a$10$x").email("a@x").role(Role.USER).build();

        assertThatThrownBy(() -> user.adjustQuota(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("adjustQuota — 음수 delta는 IllegalArgumentException")
    void adjustQuota_negativeDeltaThrows() {
        User user = User.builder()
                .username("alice").passwordHash("$2a$10$x").email("a@x").role(Role.USER).build();

        assertThatThrownBy(() -> user.adjustQuota(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
