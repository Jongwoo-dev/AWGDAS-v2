package com.jongwoo_dev.awgdas_v2.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException e, RedirectAttributes attrs) {
        attrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/admin/users";
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameExists(UsernameAlreadyExistsException e, RedirectAttributes attrs) {
        attrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/admin/users";
    }

    @ExceptionHandler(SelfModificationException.class)
    public String handleSelfModification(SelfModificationException e, RedirectAttributes attrs) {
        attrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/admin/users";
    }

    @ExceptionHandler(LastAdminException.class)
    public String handleLastAdmin(LastAdminException e, RedirectAttributes attrs) {
        attrs.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/admin/users";
    }
}
