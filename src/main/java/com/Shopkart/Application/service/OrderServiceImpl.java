package com.Shopkart.Application.service;

import com.Shopkart.Application.exceptions.APIException;
import com.Shopkart.Application.exceptions.ResourceNotFoundException;
import com.Shopkart.Application.model.*;
import com.Shopkart.Application.payload.OrderDTO;
import com.Shopkart.Application.payload.OrderItemDTO;
import com.Shopkart.Application.payload.PaymentDTO;
import com.Shopkart.Application.payload.ProductDTO;
import com.Shopkart.Application.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRespository productRespository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {

        // Getting User Cart
        Cart cart = cartRepository.findByEmail(emailId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","email",emailId);
        }

        List<CartItem> cartItems =  cart.getCartItems();
        if(cartItems.isEmpty())
            throw new APIException("Cart is Empty");

        Address address = addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        // Create a new Order with Payment Info
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod,pgPaymentId,pgStatus,pgResponseMessage,pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        // Get Items from the Cart into the order items
        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem cartItem : cartItems){
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        // Update Product Stock
        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRespository.save(product);

            // Clear the cart
            cartService.deleteProductFromCart(cart.getId(),item.getProduct().getProductId());
        });

        // Send Back the Order Summary
        OrderDTO orderDTO = modelMapper.map(savedOrder,OrderDTO.class);
        List<OrderItemDTO> orderItemDTOS = new ArrayList<>();
        for(OrderItem orderItem : orderItems){
            OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
            ProductDTO productDTO = modelMapper.map(orderItem.getProduct(), ProductDTO.class);
            productDTO.setQuantity(orderItem.getQuantity());
            orderItemDTO.setProductDTO(productDTO);
            orderItemDTOS.add(orderItemDTO);
        }

        orderDTO.setOrderItems(orderItemDTOS);
        orderDTO.setAddressId(addressId);
        return orderDTO ;
    }
}
