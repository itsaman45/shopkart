package com.Shopkart.Application.service;

import com.Shopkart.Application.payload.AddressDTO;

import java.util.List;


public interface AddressService {
    AddressDTO saveAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddress();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAdress();

    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);

    String deleteAddress(Long addressId);
}
