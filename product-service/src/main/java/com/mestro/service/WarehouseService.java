package com.mestro.service;

import com.mestro.common.exception.ResourceAlreadyExistsException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.WarehouseDTO;
import com.mestro.enums.ProductErrorCode;
import com.mestro.model.Warehouse;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        log.info("Creating warehouse: {}", warehouseDTO.getName());

        if (warehouseRepository.existsByName(warehouseDTO.getName())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.WAREHOUSE_ALREADY_EXISTS,
                    "Warehouse already exists with name: " + warehouseDTO.getName());
        }

        Warehouse warehouse = modelMapper.map(warehouseDTO, Warehouse.class);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        log.info("Warehouse created successfully with ID: {}", savedWarehouse.getId());
        return convertToDTO(savedWarehouse);
    }

    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseById(Long id) {
        log.info("Fetching warehouse with ID: {}", id);

        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse not found with ID: " + id));

        return convertToDTO(warehouse);
    }

    @Transactional(readOnly = true)
    public List<WarehouseDTO> getAllWarehouses() {
        log.info("Fetching all warehouses");

        return warehouseRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WarehouseDTO> getActiveWarehouses() {
        log.info("Fetching active warehouses");

        return warehouseRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehousesByCity(String city) {
        log.info("Fetching warehouses in city: {}", city);

        return warehouseRepository.findByCity(city).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO) {
        log.info("Updating warehouse with ID: {}", id);

        Warehouse existingWarehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse not found with ID: " + id));

        if (!existingWarehouse.getName().equals(warehouseDTO.getName())
                && warehouseRepository.existsByName(warehouseDTO.getName())) {
            throw new ResourceAlreadyExistsException(
                    ProductErrorCode.WAREHOUSE_ALREADY_EXISTS,
                    "Warehouse already exists with name: " + warehouseDTO.getName());
        }

        existingWarehouse.setName(warehouseDTO.getName());
        existingWarehouse.setLocation(warehouseDTO.getLocation());
        existingWarehouse.setCity(warehouseDTO.getCity());
        existingWarehouse.setState(warehouseDTO.getState());
        existingWarehouse.setCountry(warehouseDTO.getCountry());
        existingWarehouse.setZipCode(warehouseDTO.getZipCode());
        existingWarehouse.setCapacity(warehouseDTO.getCapacity());

        Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);

        log.info("Warehouse updated successfully with ID: {}", id);
        return convertToDTO(updatedWarehouse);
    }

    public WarehouseDTO toggleWarehouseStatus(Long id) {
        log.info("Toggling warehouse status for ID: {}", id);

        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse not found with ID: " + id));

        warehouse.setIsActive(!warehouse.getIsActive());
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        log.info("Warehouse status toggled to {} for ID: {}", updatedWarehouse.getIsActive(), id);
        return convertToDTO(updatedWarehouse);
    }

    public void deleteWarehouse(Long id) {
        log.info("Deleting warehouse with ID: {}", id);

        Warehouse warehouse = warehouseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ProductErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse not found with ID: " + id));

        warehouseRepository.delete(warehouse);
        log.info("Warehouse deleted successfully with ID: {}", id);
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        return modelMapper.map(warehouse, WarehouseDTO.class);
    }
}
