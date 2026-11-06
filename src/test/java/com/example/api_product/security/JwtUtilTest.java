package com.example.api_product.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));

    @Test
    void shouldGenerateTokenAndExtractEmail() {
        String token = jwtUtil.generateToken("user@test.com");

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("user@test.com");
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = jwtUtil.generateToken("user@test.com");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTokenIsMalformed() {
        assertThat(jwtUtil.isTokenValid("invalid-token")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsSignedWithAnotherKey() {
        JwtUtil otherJwtUtil = new JwtUtil(
                Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
        String tokenFromAnotherKey = otherJwtUtil.generateToken("user@test.com");

        assertThat(jwtUtil.isTokenValid(tokenFromAnotherKey)).isFalse();
    }
}
