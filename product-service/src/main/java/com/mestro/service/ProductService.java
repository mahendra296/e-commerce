package com.mestro.service;

import com.mestro.common.exception.ResourceAlreadyExistsException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.ProductDTO;
import com.mestro.dto.ProductImageDTO;
import com.mestro.dto.ProductInventoryDTO;
import com.mestro.enums.ProductErrorCode;
import com.mestro.model.Category;
import com.mestro.model.Product;
import com.mestro.model.ProductInventory;
import com.mestro.repository.CategoryRepository;
import com.mestro.repository.ProductImageRepository;
import com.mestro.repository.ProductInventoryRepository;
import com.mestro.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final ModelMapper modelMapper;

    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check if product with same SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.PRODUCT_SKU_EXISTS,
                    "Product with SKU '" + productDTO.getSku() + "' already exists");
        }

        // Validate category exists
        Category category = categoryRepository
                .findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND,
                        "Category not found with ID: " + productDTO.getCategoryId()));

        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository
                .findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));

        return convertToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);

        Product product = productRepository
                .findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with SKU: " + sku));

        return convertToDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");

        return productRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getActiveProducts() {
        log.info("Fetching active products");

        return productRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category ID: {}", categoryId);

        // Validate category exists
        categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + categoryId));

        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        return productRepository.searchByName(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));

        // Check if new SKU conflicts with another product
        if (!existingProduct.getSku().equals(productDTO.getSku())
                && productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.PRODUCT_SKU_EXISTS,
                    "Product with SKU '" + productDTO.getSku() + "' already exists");
        }

        // Update category if changed
        if (!existingProduct.getCategory().getId().equals(productDTO.getCategoryId())) {
            Category category = categoryRepository
                    .findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ProductErrorCode.CATEGORY_NOT_FOUND,
                            "Category not found with ID: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        existingProduct.setSku(productDTO.getSku());
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDiscountPercentage(productDTO.getDiscountPercentage());
        existingProduct.setTaxRate(productDTO.getTaxRate());
        existingProduct.setWeight(productDTO.getWeight());
        existingProduct.setDimensions(productDTO.getDimensions());

        if (productDTO.getIsActive() != null) {
            existingProduct.setIsActive(productDTO.getIsActive());
        }

        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with ID: {}", id);
        return convertToDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));

        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", id);
    }

    public ProductDTO toggleProductStatus(Long id) {
        log.info("Toggling status for product with ID: {}", id);

        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + id));

        product.setIsActive(!product.getIsActive());
        Product updatedProduct = productRepository.save(product);

        log.info("Product status toggled successfully. New status: {}", updatedProduct.getIsActive());
        return convertToDTO(updatedProduct);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        productDTO.setCategoryId(product.getCategory().getId());
        productDTO.setCategoryName(product.getCategory().getName());

        // Map images
        List<ProductImageDTO> images = productImageRepository.findByProductId(product.getId()).stream()
                .map(image -> modelMapper.map(image, ProductImageDTO.class))
                .collect(Collectors.toList());
        productDTO.setImages(images);

        // Map inventories
        List<ProductInventoryDTO> inventories = productInventoryRepository.findByProductId(product.getId()).stream()
                .map(this::convertInventoryToDTO)
                .collect(Collectors.toList());
        productDTO.setInventories(inventories);

        return productDTO;
    }

    private ProductInventoryDTO convertInventoryToDTO(ProductInventory inventory) {
        ProductInventoryDTO dto = modelMapper.map(inventory, ProductInventoryDTO.class);
        dto.setTotalQuantity(inventory.getTotalQuantity());
        dto.setIsLowStock(inventory.isLowStock());
        return dto;
    }
}
