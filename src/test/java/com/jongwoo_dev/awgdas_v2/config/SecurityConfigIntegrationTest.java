package com.jongwoo_dev.awgdas_v2.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비인증 사용자가 홈에 접근할 수 있다")
    void unauthenticatedUser_canAccessHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비인증 사용자가 로그인 페이지에 접근할 수 있다")
    void unauthenticatedUser_canAccessLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비인증 사용자가 보호된 경로 접근 시 로그인으로 리다이렉트된다")
    void unauthenticatedUser_redirectedToLogin() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("인증된 사용자가 로그아웃할 수 있다")
    void authenticatedUser_canLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(user("dev").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("ROLE_ADMIN은 /admin에 접근할 수 있다")
    void admin_canAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_USER가 /admin 접근 시 403")
    void user_forbiddenOnAdmin() throws Exception {
        mockMvc.perform(get("/admin").with(user("alice").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("익명 사용자가 /admin 접근 시 로그인으로 리다이렉트")
    void anonymous_redirectedFromAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
