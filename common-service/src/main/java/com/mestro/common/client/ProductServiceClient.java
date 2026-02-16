package com.mestro.common.client;

import com.mestro.common.dto.ApiResponse;
import com.mestro.common.dto.InventoryResponse;
import com.mestro.common.dto.ProductResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/products/sku/{sku}")
    ApiResponse<ProductResponse> getProductBySku(@PathVariable("sku") String sku);

    @GetMapping("/api/v1/inventories/product/{productId}")
    ApiResponse<List<InventoryResponse>> getInventoriesByProduct(@PathVariable("productId") Long productId);

    @GetMapping("/api/v1/inventories/product/{productId}/total")
    ApiResponse<Integer> getTotalAvailableQuantity(@PathVariable("productId") Long productId);

    @GetMapping("/api/v1/inventories/product/{productId}/warehouse/{warehouseId}")
    ApiResponse<InventoryResponse> getInventoryByProductAndWarehouse(
            @PathVariable("productId") Long productId, @PathVariable("warehouseId") Long warehouseId);

    @PutMapping("/api/v1/inventories/product/{productId}/reserve")
    ApiResponse<InventoryResponse> reserveByProductId(
            @PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);

    @PutMapping("/api/v1/inventories/product/{productId}/warehouse/{warehouseId}/reserve")
    ApiResponse<InventoryResponse> reserveByProductAndWarehouse(
            @PathVariable("productId") Long productId,
            @PathVariable("warehouseId") Long warehouseId,
            @RequestParam("quantity") Integer quantity);

    @PutMapping("/api/v1/inventories/product/{productId}/release")
    ApiResponse<InventoryResponse> releaseByProductId(
            @PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);

    @PutMapping("/api/v1/inventories/product/{productId}/warehouse/{warehouseId}/release")
    ApiResponse<InventoryResponse> releaseByProductAndWarehouse(
            @PathVariable("productId") Long productId,
            @PathVariable("warehouseId") Long warehouseId,
            @RequestParam("quantity") Integer quantity);
}
