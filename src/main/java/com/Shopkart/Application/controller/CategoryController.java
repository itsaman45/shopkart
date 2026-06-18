package com.Shopkart.Application.controller;

import com.Shopkart.Application.config.AppConstants;
import com.Shopkart.Application.payload.CategoryDTO;
import com.Shopkart.Application.payload.CategoryResponse;
import com.Shopkart.Application.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/public/category")
    public ResponseEntity<CategoryResponse> getCategory(
            @RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder

    ){
        CategoryResponse categoryResponse = categoryService.getCategory(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
    }

    @PostMapping("/public/category")
    public ResponseEntity<CategoryDTO> addCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/category/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
        CategoryDTO deletedCategory =  categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }

    @PutMapping("/public/category/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid  @RequestBody CategoryDTO categoryDTO,
                                                 @PathVariable Long categoryId){

            CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO,categoryId);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.OK);
    }

}
