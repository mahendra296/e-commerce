package com.mestro.controller;

import com.mestro.dto.ApiResponse;
import com.mestro.dto.CustomerAddressDTO;
import com.mestro.service.CustomerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@RequiredArgsConstructor
@Tag(name = "Customer Address", description = "Customer address management APIs")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    @Operation(summary = "Create a new address", description = "Creates a new address for the specified customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Address created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> createAddress(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long customerId,
            @RequestBody CustomerAddressDTO addressDTO) {
        CustomerAddressDTO createdAddress = addressService.createAddress(customerId, addressDTO);
        ApiResponse<CustomerAddressDTO> response = ApiResponse.success("Address created successfully", createdAddress);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all addresses for a customer", description = "Retrieves all addresses associated with the specified customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAddressDTO>>> getAddressesByCustomerId(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long customerId) {
        List<CustomerAddressDTO> addresses = addressService.getAddressesByCustomerId(customerId);
        ApiResponse<List<CustomerAddressDTO>> response =
                ApiResponse.success("Addresses retrieved successfully", addresses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get address by ID", description = "Retrieves a specific address by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> getAddressById(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long customerId,
            @Parameter(description = "Address ID", required = true, example = "1")
            @PathVariable Long addressId) {
        CustomerAddressDTO address = addressService.getAddressById(addressId);
        ApiResponse<CustomerAddressDTO> response = ApiResponse.success("Address retrieved successfully", address);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an address", description = "Updates an existing address with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> updateAddress(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long customerId,
            @Parameter(description = "Address ID", required = true, example = "1")
            @PathVariable Long addressId,
            @RequestBody CustomerAddressDTO addressDTO) {
        CustomerAddressDTO updatedAddress = addressService.updateAddress(addressId, addressDTO);
        ApiResponse<CustomerAddressDTO> response = ApiResponse.success("Address updated successfully", updatedAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an address", description = "Deletes an address by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long customerId,
            @Parameter(description = "Address ID", required = true, example = "1")
            @PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        ApiResponse<Void> response = ApiResponse.success("Address deleted successfully", null);
        return ResponseEntity.ok(response);
    }
}
