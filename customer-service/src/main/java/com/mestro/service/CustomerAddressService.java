package com.mestro.service;

import com.mestro.dto.CustomerAddressDTO;
import java.util.List;

public interface CustomerAddressService {

    CustomerAddressDTO createAddress(Long customerId, CustomerAddressDTO addressDTO);

    CustomerAddressDTO getAddressById(Long addressId);

    List<CustomerAddressDTO> getAddressesByCustomerId(Long customerId);

    CustomerAddressDTO updateAddress(Long addressId, CustomerAddressDTO addressDTO);

    void deleteAddress(Long addressId);
}
