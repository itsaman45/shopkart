package com.Shopkart.Application.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private CartDTO cart;
    private ProductDTO productDTO;
    private Integer quantity;
    private double discount;
    private double productPrice;

}
