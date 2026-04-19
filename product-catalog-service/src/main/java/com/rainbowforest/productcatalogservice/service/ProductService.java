package com.rainbowforest.productcatalogservice.service;

import java.util.List;
import com.rainbowforest.productcatalogservice.entity.Product;

public interface ProductService {
    List<Product> getAllProduct();

    List<Product> getAllProductByCategory(String category);

    Product getProductById(Long id);

    List<Product> getAllProductsByName(String name);

    Product addProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long productId);
}