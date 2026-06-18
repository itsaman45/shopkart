package com.Shopkart.Application.service;

import com.Shopkart.Application.exceptions.APIException;
import com.Shopkart.Application.exceptions.ResourceNotFoundException;
import com.Shopkart.Application.model.Cart;
import com.Shopkart.Application.model.Category;
import com.Shopkart.Application.model.Product;
import com.Shopkart.Application.payload.CartDTO;
import com.Shopkart.Application.payload.ProductDTO;
import com.Shopkart.Application.payload.ProductResponse;
import com.Shopkart.Application.repository.CartItemRepository;
import com.Shopkart.Application.repository.CartRepository;
import com.Shopkart.Application.repository.CategoryRepository;
import com.Shopkart.Application.repository.ProductRespository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRespository productRespository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        Product product = modelMapper.map(productDTO,Product.class);

        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();
        for(Product value : products){
            if (value.getProductName().equals(product.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            product.setImage("default.png");
            product.setCategory(category);
            Double specialPrice =  product.getPrice() * (1 - product.getDiscount()/100);
            product.setSpecialPrice(specialPrice);

            Product savedProduct = productRespository.save(product);

            return modelMapper.map(savedProduct ,ProductDTO.class);
        }
        else
            throw new APIException("Product with name " + product.getProductName() + " already exists!!");
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        Sort sort;
        if(sortOrder.equalsIgnoreCase("asc"))
            sort = Sort.by(sortBy).ascending();
        else
            sort = Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> pageProducts = productRespository.findAll(pageDetails);

        List<Product> products = pageProducts.toList();
        if(products.isEmpty())
            throw new APIException("No Products Found");

        List<ProductDTO> productDTOS = new ArrayList<>();
        for(Product product: products){
            ProductDTO newProductDTO = modelMapper.map(product,ProductDTO.class);
            productDTOS.add(newProductDTO);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new APIException("Invalid Category"));

        Sort sort;
        if(sortOrder.equalsIgnoreCase("asc"))
            sort = Sort.by(sortBy).ascending();
        else
            sort = Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> pageProducts = productRespository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> products = pageProducts.toList();

        if(products.isEmpty())
            throw new APIException("No Product Found in this Category");

        List<ProductDTO> productDTOS = new ArrayList<>();
        for(Product product: products){
            ProductDTO newProductDTO = modelMapper.map(product,ProductDTO.class);
            productDTOS.add(newProductDTO);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword,Integer pageNumber,Integer pageSize,String sortBy, String sortOrder) {

        Sort sort;
        if(sortOrder.equalsIgnoreCase("asc"))
            sort = Sort.by(sortBy).ascending();
        else
            sort = Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> pageProducts = productRespository.findByProductNameLikeIgnoreCase('%' + keyword + '%',pageDetails);
        List<Product> products = pageProducts.toList();

        if(products.isEmpty())
            throw new APIException("No Product found with this Name");

        List<ProductDTO> productDTOS = new ArrayList<>();
        for(Product product: products){
            ProductDTO newProductDTO = modelMapper.map(product,ProductDTO.class);
            productDTOS.add(newProductDTO);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product savedProduct = productRespository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        Product product = modelMapper.map(productDTO,Product.class);
        savedProduct.setProductName(product.getProductName());
        savedProduct.setDescription(product.getDescription());
        savedProduct.setQuantity(product.getQuantity());
        savedProduct.setPrice(product.getPrice());
        savedProduct.setDiscount(product.getDiscount());
        savedProduct.setSpecialPrice(product.getPrice() * (1 - product.getDiscount()/100));

        Product updatedProduct = productRespository.save(savedProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
            List<ProductDTO> products =  cart.getCartItems().stream().map(p->{
                return modelMapper.map(p.getProduct(),ProductDTO.class);
            }).collect(Collectors.toUnmodifiableList());

            cartDTO.setProducts(products);
            return cartDTO;
        }).collect(Collectors.toUnmodifiableList());


        cartDTOS.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(),productId));

        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRespository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getId(),productId));

        productRespository.deleteById(productId);
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get Product from DB
        Product productFromDB = productRespository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        // Upload image to server
        // Get the file name of uploaded image
        String fileName = fileService.uploadImage(path,image);

        // Updating the new file name to the product
        productFromDB.setImage(fileName);

        // Save updated product
        Product updatedProduct = productRespository.save(productFromDB);

        // return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct,ProductDTO.class);

    }
}
