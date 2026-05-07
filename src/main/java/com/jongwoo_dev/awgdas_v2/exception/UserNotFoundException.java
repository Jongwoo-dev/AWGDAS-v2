package com.jongwoo_dev.awgdas_v2.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found: id=" + userId);
    }
}
