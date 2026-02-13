package com.mestro.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer reorderLevel;
    private Integer totalQuantity;
    private Boolean isLowStock;
}
