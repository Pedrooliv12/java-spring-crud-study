package com.example.api_product;

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
	private String token;


    @BeforeEach
	void setUp() {
		token = jwtUtil.generateToken("test");
	}

	@Test
	void shouldReturnEmptyListWhenNoProducts() throws Exception {
		mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void shouldCreateProductAndReturn201() throws  Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Keyboard"));
	}

	@Test
	void shouldReturn404WhenProductNotFound() throws Exception {
		mockMvc.perform(get("/products/999").header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

	@Test
	void shouldReturn400WhenNameIsBlank()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name").exists());
	}

	@Test
	void shouldReturn400WhenPriceIsNull()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": null, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void shouldReturn201WhenStockQuantityIsZero()  throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 0}"))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldReturn400WhenPriceIsNegative() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": -49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.price").exists());
	}

	@Test
	void shouldReturn400WhenStockQuantityIsNegative() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": -10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.stockQuantity").exists());
	}

	@Test
	void shouldReturn400WhenStockQuantityIsNull() throws Exception {
		mockMvc.perform(post("/products").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.stockQuantity").exists());
	}

	@Test
	void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
		mockMvc.perform(put("/products/999").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

	@Test
	void shouldReturn400WhenUpdatingWithInvalidBody() throws Exception {
		mockMvc.perform(put("/products/999").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name").exists());
	}

	@Test
	void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
		mockMvc.perform(delete("/products/999").header("Authorization", "Bearer " + token) )
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Product not found with id: 999"));
	}

}
