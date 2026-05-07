package com.jongwoo_dev.awgdas_v2.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordGeneratorTest {

    @Test
    @DisplayName("기본 생성은 12자")
    void generate_defaultIs12Chars() {
        String pw = PasswordGenerator.generate();

        assertThat(pw).hasSize(12);
    }

    @Test
    @DisplayName("지정 길이로 생성 가능")
    void generate_respectsLength() {
        assertThat(PasswordGenerator.generate(8)).hasSize(8);
        assertThat(PasswordGenerator.generate(20)).hasSize(20);
    }

    @Test
    @DisplayName("영숫자 charset 외 문자는 포함되지 않음")
    void generate_alphanumericOnly() {
        String pw = PasswordGenerator.generate(50);

        assertThat(pw).matches("[A-Za-z0-9]+");
    }

    @Test
    @DisplayName("매 호출마다 결과가 동일하지 않음 (SecureRandom)")
    void generate_isNotConstant() {
        Set<String> samples = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            samples.add(PasswordGenerator.generate(12));
        }

        assertThat(samples).hasSizeGreaterThan(45);
    }

    @Test
    @DisplayName("0 또는 음수 길이는 IllegalArgumentException")
    void generate_rejectsNonPositiveLength() {
        assertThatThrownBy(() -> PasswordGenerator.generate(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PasswordGenerator.generate(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
