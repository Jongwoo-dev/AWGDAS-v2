package com.jongwoo_dev.awgdas_v2.repository;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("seed된 admin을 username으로 조회할 수 있다")
    void findByUsername_returnsSeededAdmin() {
        Optional<User> found = userRepository.findByUsername("admin");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(found.get().getEmail()).isEqualTo("admin@awgdas.local");
        assertThat(found.get().isEnabled()).isTrue();
        assertThat(found.get().getQuota()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 username 조회 시 빈 Optional 반환")
    void findByUsername_returnsEmptyForUnknown() {
        Optional<User> found = userRepository.findByUsername("nonexistent-user");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("새 사용자 저장 후 조회할 수 있다 (createdAt/updatedAt 자동 채움, enabled 기본 true, quota 기본 10)")
    void save_persistsUserWithTimestamps() {
        User newUser = User.builder()
                .username("alice")
                .passwordHash("$2a$10$dummy")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getQuota()).isEqualTo(10);
    }

    @Test
    @DisplayName("enabled 필터링 (true/false 분리)")
    void findByEnabled_filtersByStatus() {
        userRepository.save(User.builder()
                .username("active-user").passwordHash("$2a$10$x").email("a@x").role(Role.USER).enabled(true).build());
        userRepository.save(User.builder()
                .username("disabled-user").passwordHash("$2a$10$x").email("d@x").role(Role.USER).enabled(false).build());

        Page<User> active = userRepository.findByEnabled(true, PageRequest.of(0, 20));
        Page<User> disabled = userRepository.findByEnabled(false, PageRequest.of(0, 20));

        assertThat(active.getContent()).extracting(User::getUsername)
                .contains("admin", "active-user")
                .doesNotContain("disabled-user");
        assertThat(disabled.getContent()).extracting(User::getUsername)
                .containsExactly("disabled-user");
    }

    @Test
    @DisplayName("role 필터링 (ADMIN/USER 분리)")
    void findByRole_filtersByRole() {
        userRepository.save(User.builder()
                .username("u1").passwordHash("$2a$10$x").email("u1@x").role(Role.USER).build());

        Page<User> admins = userRepository.findByRole(Role.ADMIN, PageRequest.of(0, 20));
        Page<User> users = userRepository.findByRole(Role.USER, PageRequest.of(0, 20));

        assertThat(admins.getContent()).extracting(User::getUsername).containsExactly("admin");
        assertThat(users.getContent()).extracting(User::getUsername).containsExactly("u1");
    }

    @Test
    @DisplayName("role + enabled 조합 필터링")
    void findByRoleAndEnabled_filtersByBoth() {
        userRepository.save(User.builder()
                .username("active-user").passwordHash("$2a$10$x").email("a@x").role(Role.USER).enabled(true).build());
        userRepository.save(User.builder()
                .username("disabled-user").passwordHash("$2a$10$x").email("d@x").role(Role.USER).enabled(false).build());

        Page<User> activeUsers = userRepository.findByRoleAndEnabled(Role.USER, true, PageRequest.of(0, 20));

        assertThat(activeUsers.getContent()).extracting(User::getUsername).containsExactly("active-user");
    }

    @Test
    @DisplayName("countByRoleAndEnabled — 활성 admin 수 카운트")
    void countByRoleAndEnabled_countsCorrectly() {
        long activeAdmins = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
        long disabledAdmins = userRepository.countByRoleAndEnabled(Role.ADMIN, false);

        assertThat(activeAdmins).isEqualTo(1L);
        assertThat(disabledAdmins).isZero();
    }
}
