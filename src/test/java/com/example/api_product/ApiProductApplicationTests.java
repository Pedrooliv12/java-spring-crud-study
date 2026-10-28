package com.example.api_product;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiProductApplicationTests {
	// Piramide de testes
	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnEmptyListWhenNoProducts() throws Exception {
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void shouldCreateProductAndReturn201() throws  Exception {
		mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Keyboard\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Keyboard"));
	}

	@Test
	void shouldReturn404WhenProductNotFound() throws Exception {
		mockMvc.perform(get("/products/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn400WhenNameIsBlank()  throws Exception {
		mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\", \"price\": 49.99, \"stockQuantity\": 10}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void contextLoads() {
	}

}
