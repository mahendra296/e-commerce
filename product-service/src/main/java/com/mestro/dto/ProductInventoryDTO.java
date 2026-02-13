package com.mestro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryDTO {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available cannot be negative")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Quantity reserved cannot be negative")
    private Integer quantityReserved;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private Integer totalQuantity;

    private Boolean isLowStock;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
