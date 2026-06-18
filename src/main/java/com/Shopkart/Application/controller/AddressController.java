package com.Shopkart.Application.controller;


import com.Shopkart.Application.payload.AddressDTO;
import com.Shopkart.Application.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/address")
    public ResponseEntity<AddressDTO> saveAddress(@Valid @RequestBody AddressDTO addressDTO){

        AddressDTO savedAddress = addressService.saveAddress(addressDTO);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressDTO>> getAddress(){
        List<AddressDTO> addressDTOS = addressService.getAllAddress();
        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long addressId){
        AddressDTO addressDTO = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDTO,HttpStatus.OK);
    }

    @GetMapping("/address/user")
    public ResponseEntity<List<AddressDTO>> getAddressByUser(){
        List<AddressDTO> addressDTOS = addressService.getUserAdress();
        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId,@RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressId,addressDTO);
        return new ResponseEntity<>(updatedAddressDTO,HttpStatus.OK);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){
        String status = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(status,HttpStatus.OK);
    }


}
