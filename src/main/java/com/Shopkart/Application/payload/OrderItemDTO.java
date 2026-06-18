package com.Shopkart.Application.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.NIP;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private ProductDTO productDTO;
    private Integer quantity;
    private double price;
    private double orderedProductPrice;
}

