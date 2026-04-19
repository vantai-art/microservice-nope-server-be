package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach; // JUnit 5
import org.junit.jupiter.api.Test; // JUnit 5
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests { // JUnit 5: Không cần public, không cần @RunWith

    private static final String PRODUCT_NAME = "test";
    private static final Long PRODUCT_ID = 5L;
    private static final String PRODUCT_CATEGORY = "testCategory";
    private List<Product> products;
    private Product product;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach // Thay cho @Before
    void setUp() {
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setProductName(PRODUCT_NAME);
        product.setCategory(PRODUCT_CATEGORY);
        products = new ArrayList<>();
        products.add(product);
    }

    @Test
    void get_all_products_controller_should_return200_when_validRequest() throws Exception {
        // when
        when(productService.getAllProduct()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Sửa lỗi UTF8
                .andExpect(jsonPath("$[0].id").value(PRODUCT_ID))
                .andExpect(jsonPath("$[0].productName").value(PRODUCT_NAME));

        verify(productService, times(1)).getAllProduct();
    }

    @Test
    void get_all_products_controller_should_return404_when_productList_isEmpty() throws Exception {
        // given
        List<Product> emptyList = new ArrayList<>();

        // when
        when(productService.getAllProduct()).thenReturn(emptyList);

        // then
        mockMvc.perform(get("/products"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getAllProduct();
    }

    @Test
    void get_all_product_by_category_controller_should_return200_when_validRequest() throws Exception {
        // when
        when(productService.getAllProductByCategory(PRODUCT_CATEGORY)).thenReturn(products);

        // then
        mockMvc.perform(get("/products").param("category", PRODUCT_CATEGORY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PRODUCT_ID))
                .andExpect(jsonPath("$[0].category").value(PRODUCT_CATEGORY));

        verify(productService, times(1)).getAllProductByCategory(anyString());
    }

    @Test
    void get_all_product_by_category_controller_should_return404_when_productList_isEmpty() throws Exception {
        // given
        List<Product> emptyList = new ArrayList<>();

        // when - Fix lại mock đúng method category
        when(productService.getAllProductByCategory(anyString())).thenReturn(emptyList);

        // then
        mockMvc.perform(get("/products").param("category", PRODUCT_CATEGORY))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getAllProductByCategory(anyString());
    }

    @Test
    void get_one_product_by_id_controller_should_return200_when_validRequest() throws Exception {
        // when
        when(productService.getProductById(anyLong())).thenReturn(product);

        // then
        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.productName").value(PRODUCT_NAME));

        verify(productService, times(1)).getProductById(PRODUCT_ID);
    }

    @Test
    void get_one_product_by_id_controller_should_return404_when_product_isNotExist() throws Exception {
        // when
        when(productService.getProductById(anyLong())).thenReturn(null);

        // then
        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(PRODUCT_ID);
    }
}