package com.Shopkart.Application.service;

import com.Shopkart.Application.payload.CartDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getUserCart(String email, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    @Transactional
    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}
