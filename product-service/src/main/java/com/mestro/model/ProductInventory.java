package com.mestro.model;

import com.mestro.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    public Integer getTotalQuantity() {
        return quantityAvailable + quantityReserved;
    }

    public boolean isLowStock() {
        return quantityAvailable <= reorderLevel;
    }
}
