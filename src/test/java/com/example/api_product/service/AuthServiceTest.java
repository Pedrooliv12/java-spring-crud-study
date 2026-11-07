package com.example.api_product.service;

import com.example.api_product.dto.LoginRequestDTO;
import com.example.api_product.dto.RegisterRequestDTO;
import com.example.api_product.exception.EmailAlreadyExistsException;
import com.example.api_product.exception.InvalidCredentialsException;
import com.example.api_product.model.Role;
import com.example.api_product.model.User;
import com.example.api_product.repository.UserRepository;
import com.example.api_product.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldThrowAndNotSaveWhenEmailAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("user@test.com", "password123");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldSaveUserWithEncodedPassword() {
        RegisterRequestDTO dto = new RegisterRequestDTO("user@test.com", "password123");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtUtil.generateToken("user@test.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        LoginRequestDTO dto = new LoginRequestDTO("user@test.com", "password123");
        User user = new User("user@test.com", "encoded-password");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("user@test.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.login(dto);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        LoginRequestDTO dto = new LoginRequestDTO("user@test.com", "wrongpassword");
        User user = new User("user@test.com", "encoded-password");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void shouldThrowWhenEmailDoesNotExist() {
        LoginRequestDTO dto = new LoginRequestDTO("unknown@test.com", "password123");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
