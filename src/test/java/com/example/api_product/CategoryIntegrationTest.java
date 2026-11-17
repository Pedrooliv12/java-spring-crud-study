package com.example.api_product;

import com.example.api_product.model.Role;
import com.example.api_product.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestContainersConfiguration.class)
public class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateToken("admin@test.com", Role.ADMIN);
        userToken  = jwtUtil.generateToken("user@test.com",  Role.USER);
    }

    private String categoryJson(String name) {
        return "{\"name\": \"%s\"}".formatted(name);
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnCategoryListForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn401WhenListingCategoriesWithoutToken() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateCategoryAndFindItById() throws Exception {
        String response = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Electronics")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/categories/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void shouldReturn409WhenCreatingDuplicateCategoryName() throws Exception {
        mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Electronics")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Electronics")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Category already exists with name Electronics"));
    }

    @Test
    void shouldReturn403WhenUserTriesToCreateCategory() throws Exception {
        mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Electronics")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400WhenCreatingCategoryWithBlankName() throws Exception {
        mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateCategoryName() throws Exception {
        String response = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Electronics")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(put("/categories/" + id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Audio")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Audio"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentCategory() throws Exception {
        mockMvc.perform(put("/categories/999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Audio")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category not found with id: 999"));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void shouldDeleteCategoryAndReturn404Afterwards() throws Exception {
        String response = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Electronics")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/categories/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category not found with id: " + id));
    }

    @Test
    void shouldReturn403WhenUserTriesToDeleteCategory() throws Exception {
        mockMvc.perform(delete("/categories/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
