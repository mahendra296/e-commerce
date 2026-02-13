package com.mestro.controller;

import com.mestro.common.dto.ApiResponse;
import com.mestro.dto.ProductDTO;
import com.mestro.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to create product: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", createdProduct));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        log.info("REST request to get product by ID: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductBySku(@PathVariable String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        log.info("REST request to get all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getActiveProducts() {
        log.info("REST request to get active products");
        List<ProductDTO> products = productService.getActiveProducts();
        return ResponseEntity.ok(ApiResponse.success("Active products retrieved successfully", products));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("REST request to get products by category ID: {}", categoryId);
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String keyword) {
        log.info("REST request to search products with keyword: {}", keyword);
        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Products search completed successfully", products));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to update product with ID: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleProductStatus(@PathVariable Long id) {
        log.info("REST request to toggle status for product ID: {}", id);
        ProductDTO updatedProduct = productService.toggleProductStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Product status toggled successfully", updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
