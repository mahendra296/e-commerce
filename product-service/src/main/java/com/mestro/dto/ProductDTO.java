package com.mestro.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    private String brand;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have maximum 8 digits before decimal and 2 after")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    private BigDecimal taxRate;

    @DecimalMin(value = "0.00", message = "Weight cannot be negative")
    private BigDecimal weight;

    private String dimensions;

    private Boolean isActive;

    private List<ProductImageDTO> images;

    private List<ProductInventoryDTO> inventories;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
