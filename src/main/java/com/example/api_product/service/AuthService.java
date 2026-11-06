package com.example.api_product.service;

import com.example.api_product.dto.LoginRequestDTO;
import com.example.api_product.dto.RegisterRequestDTO;
import com.example.api_product.exception.EmailAlreadyExistsException;
import com.example.api_product.exception.InvalidCredentialsException;
import com.example.api_product.model.User;
import com.example.api_product.repository.UserRepository;
import com.example.api_product.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        userRepository.save(user);
        return jwtUtil.generateToken(user.getEmail());
    }

    public String login(LoginRequestDTO dto) {
        return userRepository.findByEmail(dto.email())
                .filter(user -> passwordEncoder.matches(dto.password(), user.getPassword()))
                .map(user -> jwtUtil.generateToken(user.getEmail()))
                .orElseThrow(() -> new InvalidCredentialsException());
    }

}
