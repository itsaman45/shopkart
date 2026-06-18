package com.Shopkart.Application.repository;

import com.Shopkart.Application.model.Category;
import com.Shopkart.Application.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRespository extends JpaRepository<Product,Long>{

    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);

    Page<Product> findByProductNameLikeIgnoreCase(String keyword, Pageable pageDetails);

    Product findByProductName(String productName);
}
