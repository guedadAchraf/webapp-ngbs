package com.alten.shop.dto;

import com.alten.shop.entity.Product.InventoryStatus;
import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String image;
    private String category;
    private Double price = 0.0;
    private Integer quantity = 0;
    private String internalReference;
    private Long shellId;
    private InventoryStatus inventoryStatus = InventoryStatus.INSTOCK;
    private Integer rating = 0;
    private Long createdAt;
    private Long updatedAt;
} 