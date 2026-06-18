package com.Shopkart.Application.service;

import com.Shopkart.Application.exceptions.APIException;
import com.Shopkart.Application.exceptions.ResourceNotFoundException;
import com.Shopkart.Application.model.Category;
import com.Shopkart.Application.payload.CategoryDTO;
import com.Shopkart.Application.payload.CategoryResponse;
import com.Shopkart.Application.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
//    List<Category> categories = new ArrayList<>();
//    private Long id = 101L;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getCategory(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        Sort sort;
        if(sortOrder.equalsIgnoreCase("asc"))
            sort = Sort.by(sortBy).ascending();
        else
            sort = Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Category> pageCategory = categoryRepository.findAll(pageDetails);

        List<Category> categories = pageCategory.toList();

        if(categories.isEmpty())
            throw new APIException("No Category Found");

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(pageCategory.getNumber());
        categoryResponse.setPageSize(pageCategory.getSize());
        categoryResponse.setTotalPages(pageCategory.getTotalPages());
        categoryResponse.setTotalElements(pageCategory.getTotalElements());
        categoryResponse.setLastPage(pageCategory.isLast());


        return categoryResponse;
    }



    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
//        category.setCategoryId(id);
//        id++;

        Category category = modelMapper.map(categoryDTO,Category.class);

        Category findCategory = categoryRepository.findByCategoryName(category.getCategoryName());

        if(findCategory != null){
            throw new APIException("Category with name " + category.getCategoryName() + " already Exists!!");
        }

        Category savedCategory =  categoryRepository.save(category);
        return  modelMapper.map(savedCategory, CategoryDTO.class);
    }





    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(()->new ResourceNotFoundException("category","categoryId",categoryId));


       CategoryDTO deletedCategory = modelMapper.map(category,CategoryDTO.class);
       categoryRepository.delete(category);

        return deletedCategory;

//        List<Category> categories = categoryRepository.findAll();
//        Category category =  categories.stream().
//                filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Category Not Found"));
//
//        categoryRepository.delete(category);
//        return "Category with " + categoryId + " deleted!!";
    }






    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {

        Category category = modelMapper.map(categoryDTO,Category.class);

        Category cat = categoryRepository.findByCategoryName(category.getCategoryName());
        if(cat != null){
            throw new APIException("Category with name " + category.getCategoryName() + " already exists!!!");
        }

        Category savedCategory = categoryRepository.findById(categoryId).
                orElseThrow(()->new ResourceNotFoundException("category","categoryId",categoryId));

        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);






//        List<Category> categories = categoryRepository.findAll();
//        Optional<Category> optionalCategory = categories.stream().
//                filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        if(optionalCategory.isPresent()){
//            Category existingCategory = optionalCategory.get();
//            existingCategory.setCategoryName(category.getCategoryName());
//            Category savedCategory = categoryRepository.save(existingCategory);
//            return savedCategory;
//        }
//        else{
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Category Not Found");
//        }


    }

}
