package com.jongwoo_dev.awgdas_v2.exception;

public class SelfModificationException extends RuntimeException {

    public SelfModificationException(String operation) {
        super("Cannot " + operation + " your own account");
    }
}
