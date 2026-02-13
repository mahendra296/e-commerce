package com.mestro.controller;

import com.mestro.common.dto.ApiResponse;
import com.mestro.dto.ProductInventoryDTO;
import com.mestro.service.ProductInventoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Slf4j
public class ProductInventoryController {

    private final ProductInventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> createInventory(
            @Valid @RequestBody ProductInventoryDTO inventoryDTO) {
        log.info("REST request to create inventory for product ID: {}", inventoryDTO.getProductId());
        ProductInventoryDTO createdInventory = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory created successfully", createdInventory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> getInventoryById(@PathVariable Long id) {
        log.info("REST request to get inventory by ID: {}", id);
        ProductInventoryDTO inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", inventory));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductInventoryDTO>>> getAllInventories() {
        log.info("REST request to get all inventories");
        List<ProductInventoryDTO> inventories = inventoryService.getAllInventories();
        return ResponseEntity.ok(ApiResponse.success("Inventories retrieved successfully", inventories));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductInventoryDTO>>> getInventoriesByProduct(
            @PathVariable Long productId) {
        log.info("REST request to get inventories for product ID: {}", productId);
        List<ProductInventoryDTO> inventories = inventoryService.getInventoriesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product inventories retrieved successfully", inventories));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductInventoryDTO>>> getLowStockInventories() {
        log.info("REST request to get low stock inventories");
        List<ProductInventoryDTO> inventories = inventoryService.getLowStockInventories();
        return ResponseEntity.ok(ApiResponse.success("Low stock inventories retrieved successfully", inventories));
    }

    @GetMapping("/product/{productId}/total")
    public ResponseEntity<ApiResponse<Integer>> getTotalAvailableQuantity(@PathVariable Long productId) {
        log.info("REST request to get total available quantity for product ID: {}", productId);
        Integer totalQuantity = inventoryService.getTotalAvailableQuantity(productId);
        return ResponseEntity.ok(ApiResponse.success("Total quantity retrieved successfully", totalQuantity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> updateInventory(
            @PathVariable Long id, @Valid @RequestBody ProductInventoryDTO inventoryDTO) {
        log.info("REST request to update inventory with ID: {}", id);
        ProductInventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", updatedInventory));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> adjustQuantity(
            @PathVariable Long id, @RequestParam Integer quantity) {
        log.info("REST request to adjust inventory quantity for ID: {} by {}", id, quantity);
        ProductInventoryDTO updatedInventory = inventoryService.adjustQuantity(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Inventory quantity adjusted successfully", updatedInventory));
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> reserveQuantity(
            @PathVariable Long id, @RequestParam Integer quantity) {
        log.info("REST request to reserve {} units from inventory ID: {}", quantity, id);
        ProductInventoryDTO updatedInventory = inventoryService.reserveQuantity(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Quantity reserved successfully", updatedInventory));
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> releaseReservedQuantity(
            @PathVariable Long id, @RequestParam Integer quantity) {
        log.info("REST request to release {} reserved units from inventory ID: {}", quantity, id);
        ProductInventoryDTO updatedInventory = inventoryService.releaseReservedQuantity(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Reserved quantity released successfully", updatedInventory));
    }

    @PatchMapping("/product/{productId}/reserve")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> reserveByProductId(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        log.info("REST request to reserve {} units for product ID: {}", quantity, productId);
        ProductInventoryDTO updatedInventory = inventoryService.reserveByProductId(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Quantity reserved successfully", updatedInventory));
    }

    @PatchMapping("/product/{productId}/release")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> releaseByProductId(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        log.info("REST request to release {} reserved units for product ID: {}", quantity, productId);
        ProductInventoryDTO updatedInventory = inventoryService.releaseByProductId(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Reserved quantity released successfully", updatedInventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        log.info("REST request to delete inventory with ID: {}", id);
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory deleted successfully", null));
    }
}
