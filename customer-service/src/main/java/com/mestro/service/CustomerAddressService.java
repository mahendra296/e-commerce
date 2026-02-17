package com.mestro.service;

import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.CustomerAddressDTO;
import com.mestro.model.Customer;
import com.mestro.model.CustomerAddress;
import com.mestro.repository.CustomerAddressRepository;
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
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public CustomerAddressDTO createAddress(Long customerId, CustomerAddressDTO addressDTO) {
        log.info("Creating address for customer ID: {}", customerId);

        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        CustomerAddress address = mapToEntity(addressDTO);
        address.setCustomer(customer);

        CustomerAddress savedAddress = addressRepository.save(address);

        log.info("Address created successfully with ID: {}", savedAddress.getId());
        return mapToDTO(savedAddress);
    }

    @Transactional(readOnly = true)
    public CustomerAddressDTO getAddressById(Long addressId) {
        log.info("Fetching address with ID: {}", addressId);
        CustomerAddress address = addressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", addressId));
        return mapToDTO(address);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressDTO> getAddressesByCustomerId(Long customerId) {
        log.info("Fetching addresses for customer ID: {}", customerId);

        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return addressRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CustomerAddressDTO updateAddress(Long addressId, CustomerAddressDTO addressDTO) {
        log.info("Updating address with ID: {}", addressId);

        CustomerAddress address = addressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", addressId));

        address.setAddressType(addressDTO.getAddressType());
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setCountry(addressDTO.getCountry());
        address.setIsDefault(addressDTO.getIsDefault());

        CustomerAddress updatedAddress = addressRepository.save(address);

        log.info("Address updated successfully with ID: {}", updatedAddress.getId());
        return mapToDTO(updatedAddress);
    }

    public void deleteAddress(Long addressId) {
        log.info("Deleting address with ID: {}", addressId);

        CustomerAddress address = addressRepository
                .findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", addressId));

        addressRepository.delete(address);
        log.info("Address deleted successfully with ID: {}", addressId);
    }

    private CustomerAddress mapToEntity(CustomerAddressDTO dto) {
        return CustomerAddress.builder()
                .addressType(dto.getAddressType())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .isDefault(dto.getIsDefault())
                .build();
    }

    private CustomerAddressDTO mapToDTO(CustomerAddress address) {
        return CustomerAddressDTO.builder()
                .id(address.getId())
                .customerId(address.getCustomer().getId())
                .addressType(address.getAddressType())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
