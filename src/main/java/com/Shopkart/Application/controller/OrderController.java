package com.Shopkart.Application.controller;

import com.Shopkart.Application.payload.OrderDTO;
import com.Shopkart.Application.payload.OrderRequestDTO;
import com.Shopkart.Application.service.OrderService;
import com.Shopkart.Application.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.html.HTMLTableCaptionElement;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO){


        String emailId = authUtil.loggedInEmail();
        OrderDTO placedOrder = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(placedOrder, HttpStatus.CREATED);
    }
}
