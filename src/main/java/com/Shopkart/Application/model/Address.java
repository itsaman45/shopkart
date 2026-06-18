package com.Shopkart.Application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street Name must be atleast 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building Name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 3, message = "City Name must be atleast 3 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State Name must be atleast 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country Name must be atleast 2 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "PinCode must be atleast 6 characters")
    private String pincode;

    @ManyToOne
    @JoinColumn(name =  "user_id")
    private User user;

    public Address(String city, String street, String buildingName, String state, String country, String pincode) {
        this.city = city;
        this.street = street;
        this.buildingName = buildingName;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
    }
}
