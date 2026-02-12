package com.mestro.service;

import com.mestro.dto.CustomerDTO;
import com.mestro.exceptions.DuplicateResourceException;
import com.mestro.exceptions.ResourceNotFoundException;
import com.mestro.model.Customer;
import com.mestro.repository.CustomerRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
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

    @Override
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
