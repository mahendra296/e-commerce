package com.mestro.controller;

import com.mestro.common.dto.ApiResponse;
import com.mestro.dto.WarehouseDTO;
import com.mestro.service.WarehouseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseDTO>> createWarehouse(@Valid @RequestBody WarehouseDTO warehouseDTO) {
        log.info("REST request to create warehouse: {}", warehouseDTO.getName());
        WarehouseDTO createdWarehouse = warehouseService.createWarehouse(warehouseDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warehouse created successfully", createdWarehouse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouseById(@PathVariable Long id) {
        log.info("REST request to get warehouse by ID: {}", id);
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse retrieved successfully", warehouse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getAllWarehouses() {
        log.info("REST request to get all warehouses");
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(ApiResponse.success("Warehouses retrieved successfully", warehouses));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getActiveWarehouses() {
        log.info("REST request to get active warehouses");
        List<WarehouseDTO> warehouses = warehouseService.getActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success("Active warehouses retrieved successfully", warehouses));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getWarehousesByCity(@PathVariable String city) {
        log.info("REST request to get warehouses in city: {}", city);
        List<WarehouseDTO> warehouses = warehouseService.getWarehousesByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Warehouses retrieved successfully", warehouses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> updateWarehouse(
            @PathVariable Long id, @Valid @RequestBody WarehouseDTO warehouseDTO) {
        log.info("REST request to update warehouse with ID: {}", id);
        WarehouseDTO updatedWarehouse = warehouseService.updateWarehouse(id, warehouseDTO);
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully", updatedWarehouse));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<WarehouseDTO>> toggleWarehouseStatus(@PathVariable Long id) {
        log.info("REST request to toggle warehouse status for ID: {}", id);
        WarehouseDTO updatedWarehouse = warehouseService.toggleWarehouseStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse status toggled successfully", updatedWarehouse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        log.info("REST request to delete warehouse with ID: {}", id);
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully", null));
    }
}
