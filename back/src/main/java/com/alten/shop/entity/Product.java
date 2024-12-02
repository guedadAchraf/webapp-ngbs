package com.alten.shop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String image;
    
    private String category;
    
    @Column(nullable = false)
    private Double price = 0.0;
    
    private Integer quantity = 0;
    
    private String internalReference;
    
    private Long shellId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus inventoryStatus = InventoryStatus.INSTOCK;
    
    private Integer rating = 0;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    public enum InventoryStatus {
        INSTOCK, LOWSTOCK, OUTOFSTOCK
    }
} 