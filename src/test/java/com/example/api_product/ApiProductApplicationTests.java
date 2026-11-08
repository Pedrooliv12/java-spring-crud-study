package com.example.api_product;

import com.example.api_product.model.Category;
import com.example.api_product.model.Role;
import com.example.api_product.repository.CategoryRepository;
import com.example.api_product.security.JwtUtil;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestContainersConfiguration.class)
class ApiProductApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CategoryRepository categoryRepository;

	private String token;
	private Long categoryId;

    @BeforeEach
	void setUp() {
		token = jwtUtil.generateToken("admin@test.com", Role.ADMIN);
		categoryId = categoryRepository.save(new Category("Peripherals")).getId();
	}

	private String productJson(String name, String price, String stockQuantity) {
		return """
				{"name": "%s", "price": %s, "stockQuantity": %s, "categoryId": %d}
				""".formatted(name, price, stockQuantity, categoryId);
	}

	@Test
	void shouldReturnEmptyPageWhenNoProducts() throws Exception {
		mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void shouldLimitPageSizeToMaximum() throws Exception {
		mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("size", "1000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(100));
	}

	@Test
	void shouldReturn400WhenSortPropertyIsInvalid() throws Exception {
		mockMvc.perform(get("/products").header("Authorization", "Bearer " + token).param("sort", "xyz"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Invalid sort property: xyz"));
	}

	@Test
	void shouldCreateProductAndReturn201() throws  Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "10")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Keyboard"))
				.andExpect(jsonPath("$.category.id").value(categoryId))
				.andExpect(jsonPath("$.category.name").value("Peripherals"));
	}

	@Test
	void shouldReturn404WhenProductNotFound() throws Exception {
		mockMvc.perform(get("/products/999").header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

	@Test
	void shouldReturn400WhenNameIsBlank()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("", "49.99", "10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name").exists());
	}

	@Test
	void shouldReturn400WhenPriceIsNull()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "null", "10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void shouldReturn201WhenStockQuantityIsZero()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "0")))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldReturn400WhenPriceIsNegative() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "-49.99", "10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void shouldReturn400WhenPriceHasMoreThanTwoDecimalPlaces() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "9.999", "10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void shouldReturn400WhenStockQuantityIsNegative() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "-10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.stockQuantity").exists());
	}

	@Test
	void shouldReturn400WhenStockQuantityIsNull() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "null")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.stockQuantity").exists());
	}

	@Test
	void shouldReturn400WhenCategoryIdIsMissing() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.categoryId").exists());
	}

	@Test
	void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10, \"categoryId\": 999}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Category not found with id: 999"));
	}

	@Test
	void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
		mockMvc.perform(put("/products/999").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("Keyboard", "49.99", "10")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

	@Test
	void shouldReturn400WhenUpdatingWithInvalidBody() throws Exception {
		mockMvc.perform(put("/products/999").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(productJson("", "49.99", "10")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name").exists());
	}

	@Test
	void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
		mockMvc.perform(delete("/products/999").header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

}
