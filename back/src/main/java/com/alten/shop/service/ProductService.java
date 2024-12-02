package com.alten.shop.service;

import com.alten.shop.dto.ProductDTO;
import com.alten.shop.entity.Product;
import com.alten.shop.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProduct(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
    
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        try {
            log.debug("Creating product with data: {}", productDTO);
            
            // Validate required fields
            validateProduct(productDTO);

            // Generate unique code if not provided or ensure uniqueness
            String code = productDTO.getCode();
            if (code == null || code.isEmpty()) {
                code = generateUniqueCode();
            } else {
                // If code is provided, check if it's unique
                while (productRepository.existsByCode(code)) {
                    code = generateUniqueCode();
                }
            }
            productDTO.setCode(code);

            // Set default values for optional fields
            setDefaultValues(productDTO);

            Product product = convertToEntity(productDTO);
            
            // Set timestamps
            Instant now = Instant.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            
            product = productRepository.save(product);
            log.debug("Product created successfully with ID: {}", product.getId());
            
            return convertToDTO(product);
        } catch (Exception e) {
            log.error("Failed to create product: {}", productDTO, e);
            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        
        updateProductFields(existingProduct, productDTO);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        try {
            dto.setId(product.getId());
            dto.setCode(product.getCode());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setImage(product.getImage());
            dto.setCategory(product.getCategory());
            dto.setPrice(product.getPrice());
            dto.setQuantity(product.getQuantity());
            dto.setInternalReference(product.getInternalReference());
            dto.setShellId(product.getShellId());
            dto.setInventoryStatus(product.getInventoryStatus());
            dto.setRating(product.getRating());
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());
            
            return dto;
        } catch (Exception e) {
            log.error("Error converting entity to DTO: {}", product, e);
            throw new RuntimeException("Failed to convert product entity to DTO", e);
        }
    }
    
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        try {
            if (dto.getId() != null) {
                product.setId(dto.getId());
            }
            product.setCode(dto.getCode());
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setImage(dto.getImage());
            product.setCategory(dto.getCategory());
            product.setPrice(dto.getPrice());
            product.setQuantity(dto.getQuantity());
            product.setInternalReference(dto.getInternalReference());
            product.setShellId(dto.getShellId());
            product.setInventoryStatus(dto.getInventoryStatus());
            product.setRating(dto.getRating());
            
            return product;
        } catch (Exception e) {
            log.error("Error converting DTO to entity: {}", dto, e);
            throw new RuntimeException("Failed to convert product DTO to entity", e);
        }
    }
    
    private void updateProductFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setInternalReference(dto.getInternalReference());
        product.setShellId(dto.getShellId());
        product.setInventoryStatus(dto.getInventoryStatus());
        product.setRating(dto.getRating());
    }

    private String generateUniqueCode() {
        String code;
        do {
            // Generate a random code (9 characters: mix of letters and numbers)
            code = RandomStringUtils.randomAlphanumeric(9).toLowerCase();
        } while (productRepository.existsByCode(code));
        return code;
    }

    private void validateProduct(ProductDTO productDTO) {
        List<String> errors = new ArrayList<>();
        
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            errors.add("Product name is required");
        }
        
        if (productDTO.getPrice() == null) {
            errors.add("Product price is required");
        } else if (productDTO.getPrice() < 0) {
            errors.add("Product price cannot be negative");
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Product validation failed: " + String.join(", ", errors));
        }
    }

    private void setDefaultValues(ProductDTO productDTO) {
        if (productDTO.getInventoryStatus() == null) {
            productDTO.setInventoryStatus("INSTOCK");
        }
        
        if (productDTO.getQuantity() == null) {
            productDTO.setQuantity(0);
        }
        
        if (productDTO.getRating() == null) {
            productDTO.setRating(0);
        }
        
        if (productDTO.getShellId() == null) {
            productDTO.setShellId(0L);
        }
    }
} 