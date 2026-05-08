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
import com.jongwoo_dev.awgdas_v2.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<User> list(Role roleFilter, Boolean enabledFilter, Pageable pageable) {
        if (roleFilter != null && enabledFilter != null) {
            return userRepository.findByRoleAndEnabled(roleFilter, enabledFilter, pageable);
        }
        if (roleFilter != null) {
            return userRepository.findByRole(roleFilter, pageable);
        }
        if (enabledFilter != null) {
            return userRepository.findByEnabled(enabledFilter, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public CreatedUser create(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        String tempPassword = PasswordGenerator.generate();
        User newUser = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .email(request.email())
                .role(request.role())
                .enabled(true)
                .build();
        User saved = userRepository.save(newUser);
        return new CreatedUser(saved, tempPassword);
    }

    @Transactional
    public void update(Long id, UpdateUserRequest request, String currentUsername) {
        User user = get(id);
        if (isSelf(user, currentUsername) && user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
            throw new SelfModificationException("demote");
        }
        if (user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
            requireAnotherEnabledAdmin(user, "demote");
        }
        user.updateEmail(request.email());
        user.updateRole(request.role());
    }

    @Transactional
    public String resetPassword(Long id) {
        User user = get(id);
        String tempPassword = PasswordGenerator.generate();
        user.updatePasswordHash(passwordEncoder.encode(tempPassword));
        return tempPassword;
    }

    @Transactional
    public void toggleEnabled(Long id, String currentUsername) {
        User user = get(id);
        if (user.isEnabled()) {
            if (isSelf(user, currentUsername)) {
                throw new SelfModificationException("disable");
            }
            if (user.getRole() == Role.ADMIN) {
                requireAnotherEnabledAdmin(user, "disable");
            }
            user.disable();
        } else {
            user.enable();
        }
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        User user = get(id);
        if (isSelf(user, currentUsername)) {
            throw new SelfModificationException("delete");
        }
        if (user.getRole() == Role.ADMIN && user.isEnabled()) {
            requireAnotherEnabledAdmin(user, "delete");
        }
        userRepository.delete(user);
    }

    @Transactional
    public QuotaAdjustment incrementQuota(Long id) {
        User user = get(id);
        user.adjustQuota(1);
        return new QuotaAdjustment(user.getUsername(), user.getQuota());
    }

    private boolean isSelf(User user, String currentUsername) {
        return currentUsername != null && currentUsername.equals(user.getUsername());
    }

    private void requireAnotherEnabledAdmin(User target, String operation) {
        long enabledAdmins = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
        if (enabledAdmins <= 1) {
            throw new LastAdminException(operation);
        }
    }

    public record CreatedUser(User user, String temporaryPassword) {
    }

    public record QuotaAdjustment(String username, int currentQuota) {
    }
}
