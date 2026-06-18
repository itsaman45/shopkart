package com.Shopkart.Application.controller;

import com.Shopkart.Application.exceptions.APIException;
import com.Shopkart.Application.model.Cart;
import com.Shopkart.Application.payload.CartDTO;
import com.Shopkart.Application.repository.CartRepository;
import com.Shopkart.Application.service.CartService;
import com.Shopkart.Application.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.ReactiveOffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartRepository cartRepository;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,@PathVariable Integer quantity){
        CartDTO cartDTO = cartService.addProductToCart(productId,quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts(){
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOs,HttpStatus.FOUND);
    }

    @GetMapping("/carts/user/cart")
    public ResponseEntity<CartDTO> getCardById(){

        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findByEmail(email);
        if(cart == null)
            throw new APIException("Cart is Empty");

        Long cartId = cart.getId();
        CartDTO cartDTO = cartService.getUserCart(email,cartId);
        return new ResponseEntity<>(cartDTO,HttpStatus.OK );
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId,@PathVariable String operation){

        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.OK );
    }

    @DeleteMapping("carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,@PathVariable Long productId){
        String status = cartService.deleteProductFromCart(cartId,productId);
        return new ResponseEntity<>(status,HttpStatus.OK);
    }


}
