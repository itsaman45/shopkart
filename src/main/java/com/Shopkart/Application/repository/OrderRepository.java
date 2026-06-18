package com.Shopkart.Application.repository;

import com.Shopkart.Application.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
