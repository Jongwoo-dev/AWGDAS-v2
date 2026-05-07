package com.jongwoo_dev.awgdas_v2.controller;

import com.jongwoo_dev.awgdas_v2.domain.Role;
import com.jongwoo_dev.awgdas_v2.domain.User;
import com.jongwoo_dev.awgdas_v2.dto.CreateUserRequest;
import com.jongwoo_dev.awgdas_v2.dto.UpdateUserRequest;
import com.jongwoo_dev.awgdas_v2.dto.UserListItem;
import com.jongwoo_dev.awgdas_v2.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AdminUserService adminUserService;

    @GetMapping
    public String list(@RequestParam(value = "role", required = false) Role role,
                       @RequestParam(value = "enabled", required = false) Boolean enabled,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : DEFAULT_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "id"));
        Page<User> result = adminUserService.list(role, enabled, pageable);
        Page<UserListItem> view = result.map(UserListItem::from);
        model.addAttribute("page", view);
        model.addAttribute("roleFilter", role);
        model.addAttribute("enabledFilter", enabled);
        model.addAttribute("roles", Role.values());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("createUserRequest")) {
            model.addAttribute("createUserRequest", new CreateUserRequest("", "", Role.USER));
        }
        model.addAttribute("roles", Role.values());
        return "admin/users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CreateUserRequest createUserRequest,
                         BindingResult bindingResult,
                         RedirectAttributes attrs,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/users/form";
        }
        AdminUserService.CreatedUser created = adminUserService.create(createUserRequest);
        attrs.addFlashAttribute("message",
                "사용자 '" + created.user().getUsername() + "' 생성 완료. 임시 비밀번호: " + created.temporaryPassword());
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = adminUserService.get(id);
        if (!model.containsAttribute("updateUserRequest")) {
            model.addAttribute("updateUserRequest", new UpdateUserRequest(user.getEmail(), user.getRole()));
        }
        model.addAttribute("targetUser", UserListItem.from(user));
        model.addAttribute("roles", Role.values());
        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute UpdateUserRequest updateUserRequest,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes attrs,
                         Model model) {
        if (bindingResult.hasErrors()) {
            User user = adminUserService.get(id);
            model.addAttribute("targetUser", UserListItem.from(user));
            model.addAttribute("roles", Role.values());
            return "admin/users/edit";
        }
        adminUserService.update(id, updateUserRequest, currentUser.getUsername());
        attrs.addFlashAttribute("message", "사용자 정보 갱신 완료.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes attrs) {
        String newPassword = adminUserService.resetPassword(id);
        attrs.addFlashAttribute("message", "비밀번호 재설정 완료. 새 비밀번호: " + newPassword);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes attrs) {
        adminUserService.toggleEnabled(id, currentUser.getUsername());
        attrs.addFlashAttribute("message", "사용자 활성 상태가 변경되었습니다.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes attrs) {
        adminUserService.delete(id, currentUser.getUsername());
        attrs.addFlashAttribute("message", "사용자 삭제 완료.");
        return "redirect:/admin/users";
    }
}
