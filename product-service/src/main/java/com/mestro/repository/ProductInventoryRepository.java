package com.mestro.repository;

import com.mestro.model.ProductInventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {

    List<ProductInventory> findByProductId(Long productId);

    Optional<ProductInventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query(
            "SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId AND pi.quantityAvailable >= :quantity ORDER BY pi.quantityAvailable DESC")
    List<ProductInventory> findByProductIdWithSufficientStock(Long productId, Integer quantity);

    @Query("SELECT pi FROM ProductInventory pi WHERE pi.quantityAvailable <= pi.reorderLevel")
    List<ProductInventory> findLowStockInventories();

    @Query("SELECT SUM(pi.quantityAvailable) FROM ProductInventory pi WHERE pi.product.id = :productId")
    Integer getTotalAvailableQuantityByProductId(Long productId);

    void deleteByProductId(Long productId);
}
