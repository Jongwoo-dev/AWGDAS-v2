package com.jongwoo_dev.awgdas_v2.exception;

public class LastAdminException extends RuntimeException {

    public LastAdminException(String operation) {
        super("Cannot " + operation + " the last enabled admin account");
    }
}
