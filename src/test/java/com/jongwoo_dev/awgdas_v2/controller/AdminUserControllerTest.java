package com.jongwoo_dev.awgdas_v2.controller;

import com.jongwoo_dev.awgdas_v2.config.SecurityConfig;
import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import com.jongwoo_dev.awgdas_v2.exception.GlobalExceptionHandler;
import com.jongwoo_dev.awgdas_v2.exception.LastAdminException;
import com.jongwoo_dev.awgdas_v2.exception.SelfModificationException;
import com.jongwoo_dev.awgdas_v2.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /admin/users — ROLE_ADMIN 200")
    void list_adminCanAccess() throws Exception {
        given(adminUserService.list(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleUser("admin", Role.ADMIN, true))));

        mockMvc.perform(get("/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"));
    }

    @Test
    @DisplayName("GET /admin/users — ROLE_USER 403")
    void list_userForbidden() throws Exception {
        mockMvc.perform(get("/admin/users").with(user("alice").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/users — 익명 → /login 리다이렉트")
    void list_anonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /admin/users?role=USER&enabled=true — 필터 파라미터 전달")
    void list_passesFilters() throws Exception {
        given(adminUserService.list(eq(Role.USER), eq(true), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admin/users")
                        .param("role", "USER")
                        .param("enabled", "true")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(adminUserService).list(eq(Role.USER), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /admin/users/new — 생성 폼 200")
    void createForm_returnsForm() throws Exception {
        mockMvc.perform(get("/admin/users/new").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"));
    }

    @Test
    @DisplayName("POST /admin/users — 생성 성공 시 redirect + flash")
    void create_success() throws Exception {
        User newUser = sampleUser("alice", Role.USER, true);
        given(adminUserService.create(any())).willReturn(
                new AdminUserService.CreatedUser(newUser, "TempPass1234"));

        mockMvc.perform(post("/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "alice")
                        .param("email", "alice@example.com")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("POST /admin/users — validation 실패 시 폼 재표시")
    void create_validationFailure() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "")
                        .param("email", "not-an-email")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"));
    }

    @Test
    @DisplayName("POST /admin/users — CSRF 토큰 누락 403")
    void create_missingCsrfForbidden() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .param("username", "alice")
                        .param("email", "alice@example.com")
                        .param("role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /admin/users — ROLE_USER 403")
    void create_userForbidden() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(user("alice").roles("USER"))
                        .with(csrf())
                        .param("username", "bob")
                        .param("email", "bob@x.com")
                        .param("role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /admin/users/{id}/reset-password — flash에 새 비밀번호")
    void resetPassword_returnsNewPasswordFlash() throws Exception {
        given(adminUserService.resetPassword(1L)).willReturn("NewPass12345");

        mockMvc.perform(post("/admin/users/1/reset-password")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/toggle — redirect")
    void toggle_redirects() throws Exception {
        mockMvc.perform(post("/admin/users/1/toggle")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminUserService).toggleEnabled(1L, "admin");
    }

    @Test
    @DisplayName("POST /admin/users/{id}/delete — 자기 자신 삭제 시 errorMessage flash")
    void delete_selfBlocked() throws Exception {
        willThrow(new SelfModificationException("delete"))
                .given(adminUserService).delete(1L, "admin");

        mockMvc.perform(post("/admin/users/1/delete")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/toggle — 마지막 admin 비활성 시 errorMessage flash")
    void toggle_lastAdminBlocked() throws Exception {
        willThrow(new LastAdminException("disable"))
                .given(adminUserService).toggleEnabled(1L, "admin");

        mockMvc.perform(post("/admin/users/1/toggle")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/quota-up — redirect + message flash")
    void quotaUp_success() throws Exception {
        given(adminUserService.incrementQuota(1L))
                .willReturn(new AdminUserService.QuotaAdjustment("alice", 11));

        mockMvc.perform(post("/admin/users/1/quota-up")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("message"));

        verify(adminUserService).incrementQuota(1L);
    }

    @Test
    @DisplayName("POST /admin/users/{id}/quota-up — ROLE_USER 403")
    void quotaUp_userForbidden() throws Exception {
        mockMvc.perform(post("/admin/users/1/quota-up")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /admin/users/{id}/quota-up — CSRF 토큰 누락 403")
    void quotaUp_missingCsrfForbidden() throws Exception {
        mockMvc.perform(post("/admin/users/1/quota-up")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    private User sampleUser(String username, Role role, boolean enabled) {
        return User.builder()
                .username(username)
                .passwordHash("$2a$10$x")
                .email(username + "@example.com")
                .role(role)
                .enabled(enabled)
                .build();
    }
}
