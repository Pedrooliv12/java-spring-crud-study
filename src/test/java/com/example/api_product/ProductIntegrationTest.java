package com.example.api_product;

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
public class ProductIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    private String token;

    @BeforeEach
    public void setUp() {
        token = jwtUtil.generateToken("test");
    }

    @Test
    void shouldCreateProductAndFindItById() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    void shouldCreateProductAndUpdateIt() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(put("/products/" + id).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"MousePad\", \"price\": 9.99, \"stockQuantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.name").value("MousePad"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void shouldCreateProductAndDeleteIt() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

}
