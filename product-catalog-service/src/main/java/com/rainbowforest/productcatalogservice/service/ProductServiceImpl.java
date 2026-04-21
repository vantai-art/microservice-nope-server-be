package com.rainbowforest.productcatalogservice.service;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAllProductByCategory(String category) {
        return productRepository.findAllByCategory(category);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> getAllProductsByName(String name) {
        return productRepository.findAllByProductName(name);
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product newData) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id: " + id));
        if (newData.getProductName() != null)
            existing.setProductName(newData.getProductName());
        if (newData.getPrice() != null)
            existing.setPrice(newData.getPrice());
        if (newData.getDiscription() != null)
            existing.setDiscription(newData.getDiscription());
        if (newData.getCategory() != null)
            existing.setCategory(newData.getCategory());
        if (newData.getAvailability() > 0)
            existing.setAvailability(newData.getAvailability());
        // Cho phép xóa ảnh bằng cách set imageUrl = "" hoặc null
        existing.setImageUrl(newData.getImageUrl());
        return productRepository.save(existing);
    }

    @Override
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId))
            throw new RuntimeException("Không tìm thấy sản phẩm id: " + productId);
        productRepository.deleteById(productId);
    }
}