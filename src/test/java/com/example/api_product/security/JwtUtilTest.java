package com.example.api_product.security;

import com.example.api_product.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));

    @Test
    void shouldGenerateTokenAndExtractEmail() {
        String token = jwtUtil.generateToken("user@test.com", Role.USER);

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("user@test.com");
    }

    @Test
    void shouldGenerateTokenAndExtractRole() {
        String token = jwtUtil.generateToken("admin@test.com", Role.ADMIN);

        Role role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = jwtUtil.generateToken("user@test.com", Role.USER);

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
        String tokenFromAnotherKey = otherJwtUtil.generateToken("user@test.com", Role.USER);

        assertThat(jwtUtil.isTokenValid(tokenFromAnotherKey)).isFalse();
    }
}
