package com.Shopkart.Application.service;

import com.Shopkart.Application.exceptions.APIException;
import com.Shopkart.Application.exceptions.ResourceNotFoundException;
import com.Shopkart.Application.model.Cart;
import com.Shopkart.Application.model.CartItem;
import com.Shopkart.Application.model.Product;
import com.Shopkart.Application.payload.CartDTO;
import com.Shopkart.Application.payload.ProductDTO;
import com.Shopkart.Application.repository.CartItemRepository;
import com.Shopkart.Application.repository.CartRepository;
import com.Shopkart.Application.repository.ProductRespository;
import com.Shopkart.Application.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRespository productRespository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = findCartOrCreate();

        Product product = productRespository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(
                cart.getId(),
                productId
        );

        if(cartItem != null){
            throw new APIException("Product " + product.getProductName() + " already exists");
        }

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please make an order of the " + product.getProductName()
                    + " less than or equal to quantity " + product.getQuantity());
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setProductPrice(product.getSpecialPrice());
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice()*quantity));

        Cart savedCart = cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(savedCart,CartDTO.class);
        List<CartItem> cartItems = savedCart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                });

        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }


    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if(carts.isEmpty()){
            throw new APIException("No Carts Found");
        }

        List<CartDTO> cartDTOS = new ArrayList<>();

        for(Cart cart : carts){
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
            List<ProductDTO> productDTOS = new ArrayList<>();

            for(CartItem cartItem : cart.getCartItems()){
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                productDTOS.add(productDTO);
            }

            cartDTO.setProducts(productDTOS);
            cartDTOS.add(cartDTO);
        }

        return cartDTOS;
    }

    @Override
    public CartDTO getUserCart(String email,Long cartId) {
        Cart userCart = cartRepository.findCartByEmailAndCartId(email,cartId);

        if(userCart == null) {
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }

        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);
        List<ProductDTO> productDTOs = userCart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                    productDTO.setQuantity(cartItem.getQuantity());
                    return productDTO;
                }).collect(Collectors.toUnmodifiableList());

        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Cart userCart = cartRepository.findByEmail(authUtil.loggedInEmail());
        Long cartId = userCart.getId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));

        Product product = productRespository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName()
                    + " less than or equal to quantity " + product.getQuantity());
        }

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(cartId,productId);
        if(cartItem == null)
            throw new APIException("Product " + product.getProductName() + " is not available in cart");

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0)
            throw new APIException("The resultant quantity cannot be negative");

        if(newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        }
        else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());

            cart.setTotalPrice(cart.getTotalPrice() + quantity * cartItem.getProductPrice());
            Cart savedCart = cartRepository.save(cart);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(cartId,productId);
        if(cartItem ==  null)
            throw new ResourceNotFoundException("Product","productId",productId);

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice()*cartItem.getQuantity());
            cartItemRepository.deleteByProductIdAndCartId(cartId,productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from Cart";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));

        Product product = productRespository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(cartId,productId);

        if(cartItem == null)
            throw new APIException("Product " + product.getProductName() + " not available in Cart!!");

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice()*cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);

    }


    private Cart findCartOrCreate(){
        Cart userCart = cartRepository.findByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setUser(authUtil.loggedInUser());
        cart.setTotalPrice(0.00);

        Cart savedCart = cartRepository.save(cart);
        return savedCart;
    }

}
