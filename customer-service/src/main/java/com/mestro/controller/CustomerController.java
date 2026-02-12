package com.mestro.controller;

import com.mestro.dto.ApiResponse;
import com.mestro.dto.CustomerDTO;
import com.mestro.service.CustomerService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Create a new customer", description = "Creates a new customer record with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Customer with this email already exists")
    })
    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        ApiResponse<CustomerDTO> response = ApiResponse.success("Customer created successfully", createdCustomer);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by their unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        ApiResponse<CustomerDTO> response = ApiResponse.success("Customer retrieved successfully", customer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customer by email", description = "Retrieves a customer by their email address")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customers/email/{email}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerByEmail(
            @Parameter(description = "Customer email address", required = true, example = "john@example.com")
            @PathVariable String email) {
        CustomerDTO customer = customerService.getCustomerByEmail(email);
        ApiResponse<CustomerDTO> response = ApiResponse.success("Customer retrieved successfully", customer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        ApiResponse<List<CustomerDTO>> response = ApiResponse.success("Customers retrieved successfully", customers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a customer", description = "Updates an existing customer with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use by another customer")
    })
    @PutMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        ApiResponse<CustomerDTO> response = ApiResponse.success("Customer updated successfully", updatedCustomer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a customer", description = "Deletes a customer by their unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id) {
        customerService.deleteCustomer(id);
        ApiResponse<Void> response = ApiResponse.success("Customer deleted successfully", null);
        return ResponseEntity.ok(response);
    }
}
