package com.example.api_product;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProductAndFindItById() throws Exception {
        String response = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    void shouldCreateProductAndUpdateIt() throws Exception {
        String response = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(put("/products/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"MousePad\", \"price\": 9.99, \"stockQuantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/" + id))
                .andExpect(jsonPath("$.name").value("MousePad"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void shouldCreateProductAndDeleteIt() throws Exception {
        String response = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/products/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isNotFound());
    }

}
