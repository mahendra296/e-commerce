package com.mestro.service;

import com.mestro.common.exception.ResourceAlreadyExistsException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.CategoryDTO;
import com.mestro.enums.ProductErrorCode;
import com.mestro.model.Category;
import com.mestro.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        // Check if category with same name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.CATEGORY_ALREADY_EXISTS,
                    "Category with name '" + categoryDTO.getName() + "' already exists");
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + id));

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching all categories");

        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategories() {
        log.info("Fetching active categories");

        return categoryRepository.findByIsActive(true).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubCategories(Long parentCategoryId) {
        log.info("Fetching subcategories for parent ID: {}", parentCategoryId);

        return categoryRepository.findByParentCategoryId(parentCategoryId).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);

        Category existingCategory = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + id));

        // Check if new name conflicts with another category
        if (!existingCategory.getName().equals(categoryDTO.getName())
                && categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.CATEGORY_ALREADY_EXISTS,
                    "Category with name '" + categoryDTO.getName() + "' already exists");
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());
        existingCategory.setImageUrl(categoryDTO.getImageUrl());
        existingCategory.setParentCategoryId(categoryDTO.getParentCategoryId());

        if (categoryDTO.getIsActive() != null) {
            existingCategory.setIsActive(categoryDTO.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);

        log.info("Category updated successfully with ID: {}", id);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + id));

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot delete category with existing products. Please reassign or delete products first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    public CategoryDTO toggleCategoryStatus(Long id) {
        log.info("Toggling status for category with ID: {}", id);

        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + id));

        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category status toggled successfully. New status: {}", updatedCategory.getIsActive());
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }
}
