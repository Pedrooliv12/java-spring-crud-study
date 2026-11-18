package com.example.api_product;

import com.example.api_product.model.Category;
import com.example.api_product.model.Role;
import com.example.api_product.repository.CategoryRepository;
import com.example.api_product.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
    @Autowired
    private EntityManager entityManager;
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
                .andExpect(jsonPath("$.stockQuantity").value(10))
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
                .andExpect(jsonPath("$.stockQuantity").value(1))
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

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found with id: " + id));
    }

    @Test
    void shouldFilterProductsByNameSearch() throws Exception {
        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Mechanical Keyboard", "150.00", "10", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Optical Mouse", "50.00", "10", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("search", "key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mechanical Keyboard"));

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("search", "MOUSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Optical Mouse"));
    }

    @Test
    void shouldFilterProductsByCategory() throws Exception {
        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Headset", "80.00", "10", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Mousepad", "25.00", "10", accessoriesId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("category", accessoriesId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mousepad"));
    }

    @Test
    void shouldFilterProductsByLowStock() throws Exception {
        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Low Stock Item", "30.00", "3", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Normal Stock Item", "30.00", "20", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("lowStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Low Stock Item"));
    }

    @Test
    void shouldCombineFilters() throws Exception {
        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Gamer Keyboard", "199.99", "2", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Office Keyboard", "49.99", "15", peripheralsId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token)
                        .param("search", "Keyboard")
                        .param("category", peripheralsId.toString())
                        .param("lowStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Gamer Keyboard"));
    }
}
