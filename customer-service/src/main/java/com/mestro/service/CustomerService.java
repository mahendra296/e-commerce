package com.mestro.service;

import com.mestro.common.dto.PageResponseDTO;
import com.mestro.common.exception.DuplicateResourceException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.common.utils.GeneralUtils;
import com.mestro.dto.CustomerDTO;
import com.mestro.model.Customer;
import com.mestro.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating customer with email: {}", customerDTO.getEmail());

        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", customerDTO.getEmail());
        }

        Customer customer = mapToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return mapToDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.info("Fetching all customers");
        Page<Customer> pageCustomers = customerRepository.findAll(pageable);
        List<CustomerDTO> list =
                pageCustomers.getContent().stream().map(this::mapToDTO).toList();
        return GeneralUtils.pageableResponse(
                list,
                pageCustomers.getNumber(),
                pageCustomers.getSize(),
                pageCustomers.getTotalElements(),
                pageCustomers.getTotalPages(),
                pageCustomers.isFirst(),
                pageCustomers.isLast(),
                pageable);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        log.info("Updating customer with ID: {}", id);

        Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Check if email is being changed and if it already exists
        if (!customer.getEmail().equals(customerDTO.getEmail())
                && customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", customerDTO.getEmail());
        }

        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setDob(customerDTO.getDob());
        customer.setPhone(customerDTO.getPhone());
        customer.setGender(customerDTO.getGender());
        customer.setNotes(customerDTO.getNotes());

        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return mapToDTO(updatedCustomer);
    }

    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customerRepository.delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    private Customer mapToEntity(CustomerDTO dto) {
        return Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dob(dto.getDob())
                .phone(dto.getPhone())
                .gender(dto.getGender())
                .notes(dto.getNotes())
                .build();
    }

    private CustomerDTO mapToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .dob(customer.getDob())
                .phone(customer.getPhone())
                .gender(customer.getGender())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
