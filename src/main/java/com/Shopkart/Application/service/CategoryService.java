package com.Shopkart.Application.service;

import com.Shopkart.Application.payload.CategoryDTO;
import com.Shopkart.Application.payload.CategoryResponse;

public interface CategoryService {

    CategoryResponse getCategory(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder);
    CategoryDTO createCategory(CategoryDTO category);
    CategoryDTO deleteCategory(Long categoryId);
    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
