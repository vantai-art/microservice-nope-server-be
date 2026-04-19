package com.rainbowforest.productcatalogservice.service;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach; // JUnit 5
import org.junit.jupiter.api.Test; // JUnit 5
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals; // JUnit 5 Assertions

@ExtendWith(MockitoExtension.class) // Dùng MockitoExtension thay cho RunWith
class ProductServiceTests { // JUnit 5 không cần public

    private static final String PRODUCT_NAME = "test";
    private static final Long PRODUCT_ID = 5L;
    private static final String PRODUCT_CATEGORY = "testCategory";

    private List<Product> products;
    private Product product;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

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
    void get_all_product_test() {
        // Data preparation
        Mockito.when(productRepository.findAll()).thenReturn(products);

        // Method call
        List<Product> foundProducts = productService.getAllProduct();

        // Verification
        assertEquals(foundProducts.get(0).getProductName(), PRODUCT_NAME);
        Mockito.verify(productRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    void get_one_by_id_test() {
        // Data preparation
        // Lưu ý: Nếu Service dùng findById, mock findById trả về Optional
        // Nếu Service dùng getReferenceById (thay cho getOne), mock getReferenceById
        Mockito.when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // Method call
        Product found = productService.getProductById(PRODUCT_ID);

        // Verification
        assertEquals(found.getId(), PRODUCT_ID);
        Mockito.verify(productRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    void get_all_product_by_category_test() {
        // Data preparation
        Mockito.when(productRepository.findAllByCategory(PRODUCT_CATEGORY)).thenReturn(products);

        // Method call
        List<Product> foundProducts = productService.getAllProductByCategory(PRODUCT_CATEGORY);

        // Verification
        assertEquals(foundProducts.get(0).getCategory(), PRODUCT_CATEGORY);
        assertEquals(foundProducts.get(0).getProductName(), PRODUCT_NAME);
        Mockito.verify(productRepository, Mockito.times(1)).findAllByCategory(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    void get_all_products_by_name_test() {
        // Data preparation
        Mockito.when(productRepository.findAllByProductName(PRODUCT_NAME)).thenReturn(products);

        // Method call
        List<Product> foundProducts = productService.getAllProductsByName(PRODUCT_NAME);

        // Verification
        assertEquals(foundProducts.get(0).getProductName(), PRODUCT_NAME);
        Mockito.verify(productRepository, Mockito.times(1)).findAllByProductName(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(productRepository);
    }
}