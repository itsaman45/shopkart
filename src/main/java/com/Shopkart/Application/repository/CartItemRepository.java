package com.Shopkart.Application.repository;

import com.Shopkart.Application.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartItem findByProductIdAndCartId(Long id, Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    void deleteByProductIdAndCartId(Long cartId, Long productId);
}
