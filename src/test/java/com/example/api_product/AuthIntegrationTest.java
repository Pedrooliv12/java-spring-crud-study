package com.example.api_product;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestContainersConfiguration.class)
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterUserAndReturn201WithToken() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test\", \"password\": \"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void shouldReturn400WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldLoginAndReturn200WithToken() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldReturn401WhenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"test\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@test\", \"password\": \"testt\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void shouldReturn401WhenEmailDoesNotExist() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"test@testt\", \"password\": \"test\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

}
