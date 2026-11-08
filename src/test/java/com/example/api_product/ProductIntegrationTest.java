package com.example.api_product;

import com.example.api_product.model.Category;
import com.example.api_product.model.Role;
import com.example.api_product.repository.CategoryRepository;
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
    @Autowired
    private CategoryRepository categoryRepository;
    private String token;
    private Long peripheralsId;
    private Long accessoriesId;

    @BeforeEach
    public void setUp() {
        token = jwtUtil.generateToken("admin@test.com", Role.ADMIN);
        peripheralsId = categoryRepository.save(new Category("Peripherals")).getId();
        accessoriesId = categoryRepository.save(new Category("Accessories")).getId();
    }

    private String productJson(String name, String price, String stockQuantity, Long categoryId) {
        return """
                {"name": "%s", "price": %s, "stockQuantity": %s, "categoryId": %d}
                """.formatted(name, price, stockQuantity, categoryId);
    }

    @Test
    void shouldCreateProductAndFindItById() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "10", peripheralsId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.category.name").value("Peripherals"));
    }

    @Test
    void shouldCreateProductAndUpdateIt() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "10", peripheralsId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(put("/products/" + id).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("MousePad", "9.99", "1", accessoriesId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MousePad"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.category.name").value("Accessories"));
    }

    @Test
    void shouldCreateProductAndDeleteIt() throws Exception {
        String response = mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "10", peripheralsId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found with id: " + id));
    }

}
