package com.jongwoo_dev.awgdas_v2.repository;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
    }

    @Test
    @DisplayName("존재하지 않는 username 조회 시 빈 Optional 반환")
    void findByUsername_returnsEmptyForUnknown() {
        Optional<User> found = userRepository.findByUsername("nonexistent-user");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("새 사용자 저장 후 조회할 수 있다 (createdAt/updatedAt 자동 채움)")
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
    }
}
