package com.jongwoo_dev.awgdas_v2.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BcryptHashGenerator {

    private BcryptHashGenerator() {
    }

    public static void main(String[] args) {
        String plaintext = args.length > 0 ? args[0] : "admin123";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(plaintext));
    }
}
