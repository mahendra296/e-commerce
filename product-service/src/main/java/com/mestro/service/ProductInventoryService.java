package com.mestro.service;

import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.ProductInventoryDTO;
import com.mestro.enums.ProductErrorCode;
import com.mestro.model.Product;
import com.mestro.model.ProductInventory;
import com.mestro.model.Warehouse;
import com.mestro.repository.ProductInventoryRepository;
import com.mestro.repository.ProductRepository;
import com.mestro.repository.WarehouseRepository;
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
public class ProductInventoryService {

    private final ProductInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    public ProductInventoryDTO createInventory(ProductInventoryDTO inventoryDTO) {
        log.info("Creating inventory for product ID: {}", inventoryDTO.getProductId());

        Product product = productRepository
                .findById(inventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found with ID: " + inventoryDTO.getProductId()));

        ProductInventory inventory = modelMapper.map(inventoryDTO, ProductInventory.class);
        inventory.setProduct(product);

        if (inventoryDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository
                    .findById(inventoryDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ProductErrorCode.WAREHOUSE_NOT_FOUND,
                            "Warehouse not found with ID: " + inventoryDTO.getWarehouseId()));
            inventory.setWarehouse(warehouse);
        }

        ProductInventory savedInventory = inventoryRepository.save(inventory);

        log.info("Inventory created successfully with ID: {}", savedInventory.getId());
        return convertToDTO(savedInventory);
    }

    @Transactional(readOnly = true)
    public ProductInventoryDTO getInventoryById(Long id) {
        log.info("Fetching inventory with ID: {}", id);

        ProductInventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        return convertToDTO(inventory);
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> getInventoriesByProduct(Long productId) {
        log.info("Fetching inventories for product ID: {}", productId);

        productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId));

        return inventoryRepository.findByProductId(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> getAllInventories() {
        log.info("Fetching all inventories");

        return inventoryRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> getLowStockInventories() {
        log.info("Fetching low stock inventories");

        return inventoryRepository.findLowStockInventories().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getTotalAvailableQuantity(Long productId) {
        log.info("Fetching total available quantity for product ID: {}", productId);

        productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId));

        Integer total = inventoryRepository.getTotalAvailableQuantityByProductId(productId);
        return total != null ? total : 0;
    }

    public ProductInventoryDTO updateInventory(Long id, ProductInventoryDTO inventoryDTO) {
        log.info("Updating inventory with ID: {}", id);

        ProductInventory existingInventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        existingInventory.setQuantityAvailable(inventoryDTO.getQuantityAvailable());
        existingInventory.setQuantityReserved(inventoryDTO.getQuantityReserved());
        existingInventory.setReorderLevel(inventoryDTO.getReorderLevel());

        if (inventoryDTO.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository
                    .findById(inventoryDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ProductErrorCode.WAREHOUSE_NOT_FOUND,
                            "Warehouse not found with ID: " + inventoryDTO.getWarehouseId()));
            existingInventory.setWarehouse(warehouse);
        }

        ProductInventory updatedInventory = inventoryRepository.save(existingInventory);

        log.info("Inventory updated successfully with ID: {}", id);
        return convertToDTO(updatedInventory);
    }

    public ProductInventoryDTO adjustQuantity(Long id, Integer quantityChange) {
        log.info("Adjusting inventory quantity for ID: {} by {}", id, quantityChange);

        ProductInventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        int newQuantity = inventory.getQuantityAvailable() + quantityChange;

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getQuantityAvailable()
                    + ", Requested: " + Math.abs(quantityChange));
        }

        inventory.setQuantityAvailable(newQuantity);
        ProductInventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Inventory quantity adjusted. New quantity: {}", newQuantity);
        return convertToDTO(updatedInventory);
    }

    public ProductInventoryDTO reserveQuantity(Long id, Integer quantity) {
        log.info("Reserving {} units from inventory ID: {}", quantity, id);

        ProductInventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        if (inventory.getQuantityAvailable() < quantity) {
            throw new IllegalArgumentException("Insufficient stock to reserve. Available: "
                    + inventory.getQuantityAvailable() + ", Requested: " + quantity);
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);

        ProductInventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Quantity reserved successfully");
        return convertToDTO(updatedInventory);
    }

    public ProductInventoryDTO reserveByProductId(Long productId, Integer quantity) {
        log.info("Reserving {} units for product ID: {}", quantity, productId);

        productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId));

        List<ProductInventory> inventories =
                inventoryRepository.findByProductIdWithSufficientStock(productId, quantity);

        if (inventories.isEmpty()) {
            throw new IllegalArgumentException(
                    "Insufficient stock to reserve for product ID: " + productId + ", Requested: " + quantity);
        }

        ProductInventory inventory = inventories.get(0);
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);

        ProductInventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Quantity reserved successfully for product ID: {}", productId);
        return convertToDTO(updatedInventory);
    }

    public ProductInventoryDTO releaseReservedQuantity(Long id, Integer quantity) {
        log.info("Releasing {} reserved units from inventory ID: {}", quantity, id);

        ProductInventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        if (inventory.getQuantityReserved() < quantity) {
            throw new IllegalArgumentException("Cannot release more than reserved quantity. Reserved: "
                    + inventory.getQuantityReserved() + ", Requested: " + quantity);
        }

        inventory.setQuantityReserved(inventory.getQuantityReserved() - quantity);
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);

        ProductInventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Reserved quantity released successfully");
        return convertToDTO(updatedInventory);
    }

    public ProductInventoryDTO releaseByProductId(Long productId, Integer quantity) {
        log.info("Releasing {} reserved units for product ID: {}", quantity, productId);

        productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId));

        List<ProductInventory> inventories = inventoryRepository.findByProductId(productId);

        int remainingToRelease = quantity;
        ProductInventory lastUpdated = null;

        for (ProductInventory inventory : inventories) {
            if (remainingToRelease <= 0) break;

            int canRelease = Math.min(inventory.getQuantityReserved(), remainingToRelease);
            if (canRelease > 0) {
                inventory.setQuantityReserved(inventory.getQuantityReserved() - canRelease);
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() + canRelease);
                lastUpdated = inventoryRepository.save(inventory);
                remainingToRelease -= canRelease;
            }
        }

        if (remainingToRelease > 0) {
            throw new IllegalArgumentException("Cannot release all requested quantity for product ID: " + productId
                    + ". Short by: " + remainingToRelease);
        }

        log.info("Reserved quantity released successfully for product ID: {}", productId);
        return convertToDTO(lastUpdated);
    }

    public void deleteInventory(Long id) {
        log.info("Deleting inventory with ID: {}", id);

        ProductInventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.INVENTORY_NOT_FOUND, "Inventory not found with ID: " + id));

        inventoryRepository.delete(inventory);
        log.info("Inventory deleted successfully with ID: {}", id);
    }

    private ProductInventoryDTO convertToDTO(ProductInventory inventory) {
        ProductInventoryDTO dto = modelMapper.map(inventory, ProductInventoryDTO.class);
        dto.setProductId(inventory.getProduct().getId());
        dto.setTotalQuantity(inventory.getTotalQuantity());
        dto.setIsLowStock(inventory.isLowStock());
        if (inventory.getWarehouse() != null) {
            dto.setWarehouseId(inventory.getWarehouse().getId());
            dto.setWarehouseName(inventory.getWarehouse().getName());
        }
        return dto;
    }
}
