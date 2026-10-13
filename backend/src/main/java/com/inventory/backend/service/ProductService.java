package com.inventory.backend.service;

import com.inventory.backend.dto.request.ProductCreateRequest;
import com.inventory.backend.dto.request.ProductUpdateRequest;
import com.inventory.backend.dto.response.ProductResponse;
import com.inventory.backend.entity.Category;
import com.inventory.backend.entity.Product;
import com.inventory.backend.exception.DuplicateResourceException;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.repository.CategoryRepository;
import com.inventory.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", id));
        return ProductResponse.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query, Long categoryId, String status) {
        return productRepository.searchProducts(query, categoryId, status).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        if (request.getCategoryId() != null && !categoryRepository.existsById(request.getCategoryId())) {
            throw new ResourceNotFoundException("Category", "category_id", request.getCategoryId());
        }

        Product product = new Product(
                request.getCategoryId(),
                request.getSku().trim().toUpperCase(),
                request.getProductName().trim(),
                request.getDescription(),
                request.getUnitPrice(),
                request.getStatus() != null ? request.getStatus() : "ACTIVE"
        );

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", id));

        if (productRepository.existsBySkuAndProductIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        if (request.getCategoryId() != null && !categoryRepository.existsById(request.getCategoryId())) {
            throw new ResourceNotFoundException("Category", "category_id", request.getCategoryId());
        }

        product.setCategoryId(request.getCategoryId());
        product.setSku(request.getSku().trim().toUpperCase());
        product.setProductName(request.getProductName().trim());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated);
    }
}
