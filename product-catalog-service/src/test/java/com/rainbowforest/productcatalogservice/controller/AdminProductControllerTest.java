package com.rainbowforest.productcatalogservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test; // JUnit 5
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProductControllerTest { // JUnit 5 không cần public class và @RunWith

    private static final String PRODUCT_NAME = "test";
    private static final String PRODUCT_CATEGORY = "testCategory";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Dùng ObjectMapper có sẵn của Spring

    @MockBean
    private ProductService productService;

    @Test
    void add_product_controller_should_return201_when_product_isSaved() throws Exception {
        // given
        Product product = new Product();
        product.setProductName(PRODUCT_NAME);
        product.setCategory(PRODUCT_CATEGORY);
        
        String requestJson = objectMapper.writeValueAsString(product);
        
        // when - Sử dụng any() để mock chính xác hơn
        when(productService.addProduct(any(Product.class))).thenReturn(product);

        // then
        mockMvc.perform(post("/admin/products")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON)) // Sửa lỗi UTF8
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value(PRODUCT_NAME))
                .andExpect(jsonPath("$.category").value(PRODUCT_CATEGORY));

        verify(productService, times(1)).addProduct(any(Product.class));
        verifyNoMoreInteractions(productService);
    }
    
    @Test
    void add_product_controller_should_return400_when_product_isNull() throws Exception {
        // given
        String requestJson = ""; // Body trống tương đương với null object khi parse

        // then
        mockMvc.perform(post("/admin/products")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());    
    }
}