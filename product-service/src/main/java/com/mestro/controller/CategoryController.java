package com.mestro.controller;

import com.mestro.common.dto.ApiResponse;
import com.mestro.dto.CategoryDTO;
import com.mestro.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to create category: {}", categoryDTO.getName());
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", createdCategory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        log.info("REST request to get category by ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        log.info("REST request to get all categories");
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getActiveCategories() {
        log.info("REST request to get active categories");
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", categories));
    }

    @GetMapping("/subcategories/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getSubCategories(@PathVariable Long parentId) {
        log.info("REST request to get subcategories for parent ID: {}", parentId);
        List<CategoryDTO> subcategories = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(ApiResponse.success("Subcategories retrieved successfully", subcategories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to update category with ID: {}", id);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<CategoryDTO>> toggleCategoryStatus(@PathVariable Long id) {
        log.info("REST request to toggle status for category ID: {}", id);
        CategoryDTO updatedCategory = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Category status toggled successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        log.info("REST request to delete category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
