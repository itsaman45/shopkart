package com.Shopkart.Application.service;

import com.Shopkart.Application.exceptions.ResourceNotFoundException;
import com.Shopkart.Application.model.Address;
import com.Shopkart.Application.model.User;
import com.Shopkart.Application.payload.AddressDTO;
import com.Shopkart.Application.repository.AddressRepository;
import com.Shopkart.Application.repository.UserRepository;
import com.Shopkart.Application.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO saveAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();

        Address address = modelMapper.map(addressDTO,Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddress() {
        List<Address> addresses = addressRepository.findAll();

        List<AddressDTO> addressDTOS = addresses.stream().map(
                address -> {
                    AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
                    return addressDTO;
                }).collect(Collectors.toUnmodifiableList());

        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","addressId",addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAdress() {
        Long userId = authUtil.loggedInUserId();

        List<Address> addresses = addressRepository.findByUserUserId(userId);
        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address,AddressDTO.class))
                .collect(Collectors.toUnmodifiableList());

        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        address.setStreet(addressDTO.getStreet());
        address.setBuildingName(addressDTO.getBuildingName());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setPincode(addressDTO.getPincode());

        Address savedAddress = addressRepository.save(address);

        User user = address.getUser();
        List<Address> userAddress = user.getAddresses();
        for(Address add : userAddress){
            if(Objects.equals(add.getAddressId(), addressId)){
                userAddress.remove(add);
                break;
            }
        }

        userAddress.add(savedAddress);
        userRepository.save(user);
        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressRepository.delete(address);

        User user = authUtil.loggedInUser();
        List<Address> userAddress = user.getAddresses();
        for(Address add : userAddress){
            if(Objects.equals(add.getAddressId(), addressId)){
                userAddress.remove(add);
                break;
            }
        }
        return "Address Deleted with AddressId: " + addressId;
    }
}
