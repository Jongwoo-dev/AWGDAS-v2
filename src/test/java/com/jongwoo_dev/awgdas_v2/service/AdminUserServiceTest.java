package com.jongwoo_dev.awgdas_v2.service;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import com.jongwoo_dev.awgdas_v2.dto.CreateUserRequest;
import com.jongwoo_dev.awgdas_v2.dto.UpdateUserRequest;
import com.jongwoo_dev.awgdas_v2.exception.LastAdminException;
import com.jongwoo_dev.awgdas_v2.exception.SelfModificationException;
import com.jongwoo_dev.awgdas_v2.exception.UserNotFoundException;
import com.jongwoo_dev.awgdas_v2.exception.UsernameAlreadyExistsException;
import com.jongwoo_dev.awgdas_v2.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService service;

    @Test
    @DisplayName("create — 임시 비밀번호 발급 + BCrypt 해시 후 저장")
    void create_generatesTempPasswordAndSaves() {
        CreateUserRequest req = new CreateUserRequest("alice", "alice@example.com", Role.USER);
        given(userRepository.findByUsername("alice")).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("$2a$10$hashed");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        AdminUserService.CreatedUser result = service.create(req);

        assertThat(result.temporaryPassword()).hasSize(12).matches("[A-Za-z0-9]+");
        assertThat(result.user().getUsername()).isEqualTo("alice");
        assertThat(result.user().getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(result.user().isEnabled()).isTrue();

        ArgumentCaptor<String> rawCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(rawCaptor.capture());
        assertThat(rawCaptor.getValue()).isEqualTo(result.temporaryPassword());
    }

    @Test
    @DisplayName("create — 중복 username이면 UsernameAlreadyExistsException")
    void create_duplicateUsernameThrows() {
        CreateUserRequest req = new CreateUserRequest("admin", "x@x.com", Role.USER);
        given(userRepository.findByUsername("admin")).willReturn(Optional.of(adminUser()));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("get — 존재하지 않는 id이면 UserNotFoundException")
    void get_missingThrows() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("update — 이메일/역할 갱신")
    void update_modifiesEmailAndRole() {
        User target = userWith("bob", Role.USER, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));

        service.update(1L, new UpdateUserRequest("new@x.com", Role.USER), "admin");

        assertThat(target.getEmail()).isEqualTo("new@x.com");
        assertThat(target.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("update — 자기 자신을 ADMIN→USER로 강등 불가 (SelfModificationException)")
    void update_selfDemoteBlocked() {
        User self = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(self));

        assertThatThrownBy(() ->
                service.update(1L, new UpdateUserRequest("admin@x.com", Role.USER), "admin"))
                .isInstanceOf(SelfModificationException.class);
    }

    @Test
    @DisplayName("update — 마지막 활성 admin 강등 시 LastAdminException")
    void update_lastAdminDemoteBlocked() {
        User lastAdmin = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(lastAdmin));
        given(userRepository.countByRoleAndEnabled(Role.ADMIN, true)).willReturn(1L);

        assertThatThrownBy(() ->
                service.update(1L, new UpdateUserRequest("admin@x.com", Role.USER), "someone-else"))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    @DisplayName("resetPassword — 새 비밀번호 발급 + 해시 갱신")
    void resetPassword_generatesAndSetsHash() {
        User target = userWith("alice", Role.USER, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));
        given(passwordEncoder.encode(anyString())).willReturn("$2a$10$new");

        String newPw = service.resetPassword(1L);

        assertThat(newPw).hasSize(12);
        assertThat(target.getPasswordHash()).isEqualTo("$2a$10$new");
    }

    @Test
    @DisplayName("toggleEnabled — 활성 사용자 비활성화")
    void toggleEnabled_disablesActiveUser() {
        User target = userWith("alice", Role.USER, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));

        service.toggleEnabled(1L, "admin");

        assertThat(target.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("toggleEnabled — 비활성 사용자 활성화")
    void toggleEnabled_enablesDisabledUser() {
        User target = userWith("alice", Role.USER, false);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));

        service.toggleEnabled(1L, "admin");

        assertThat(target.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("toggleEnabled — 자기 자신 비활성화 차단")
    void toggleEnabled_selfDisableBlocked() {
        User self = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(self));

        assertThatThrownBy(() -> service.toggleEnabled(1L, "admin"))
                .isInstanceOf(SelfModificationException.class);
    }

    @Test
    @DisplayName("toggleEnabled — 마지막 활성 admin 비활성화 차단")
    void toggleEnabled_lastAdminDisableBlocked() {
        User lastAdmin = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(lastAdmin));
        given(userRepository.countByRoleAndEnabled(Role.ADMIN, true)).willReturn(1L);

        assertThatThrownBy(() -> service.toggleEnabled(1L, "someone-else"))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    @DisplayName("delete — 일반 사용자 삭제")
    void delete_removesUser() {
        User target = userWith("alice", Role.USER, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));

        service.delete(1L, "admin");

        verify(userRepository).delete(target);
    }

    @Test
    @DisplayName("delete — 자기 자신 삭제 차단")
    void delete_selfDeleteBlocked() {
        User self = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(self));

        assertThatThrownBy(() -> service.delete(1L, "admin"))
                .isInstanceOf(SelfModificationException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete — 마지막 활성 admin 삭제 차단")
    void delete_lastAdminDeleteBlocked() {
        User lastAdmin = userWith("admin", Role.ADMIN, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(lastAdmin));
        given(userRepository.countByRoleAndEnabled(Role.ADMIN, true)).willReturn(1L);

        assertThatThrownBy(() -> service.delete(1L, "other-admin"))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    @DisplayName("incrementQuota — quota +1 증가, username/현재 quota 반환")
    void incrementQuota_increasesByOne() {
        User target = userWith("alice", Role.USER, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(target));

        AdminUserService.QuotaAdjustment result = service.incrementQuota(1L);

        assertThat(target.getQuota()).isEqualTo(11);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.currentQuota()).isEqualTo(11);
    }

    @Test
    @DisplayName("incrementQuota — 사용자 없음 시 UserNotFoundException")
    void incrementQuota_userNotFoundThrows() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.incrementQuota(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User adminUser() {
        return userWith("admin", Role.ADMIN, true);
    }

    private User userWith(String username, Role role, boolean enabled) {
        return User.builder()
                .username(username)
                .passwordHash("$2a$10$x")
                .email(username + "@example.com")
                .role(role)
                .enabled(enabled)
                .build();
    }
}
