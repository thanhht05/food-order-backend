package com.thanh.foodorder.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.print.attribute.standard.PageRanges;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.thanh.foodorder.domain.Category;
import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.domain.ProductImage;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.dto.request.ProductRequestDTO;
import com.thanh.foodorder.dto.request.ProductUpdateRequestDTO;
import com.thanh.foodorder.dto.response.product.ResponseProductDTO;
import com.thanh.foodorder.repository.ProductRepository;
import com.thanh.foodorder.specification.ProductSpecification;
import com.thanh.foodorder.util.exception.CommonException;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public void saveProduct(Product product) {
        this.productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return this.productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product with id: {} not found", id);
            return new CommonException("Product with id " + id + " not found");
        });
    }

    public ResponseProductDTO createProduct(ProductRequestDTO productReq) {

        // 1. Load managed Category from DB first
        Category category = this.categoryService
                .getCategoryByName(productReq.getCategoryName());

        // 2. Check duplicate using managed Category
        boolean isDuplicate = productRepository.existsByNameAndCategory(
                productReq.getName(), category);

        if (isDuplicate) {
            throw new CommonException(
                    "Product '" + productReq.getName() + "' already exists in this category");
        }

        Product product = new Product();
        product.setName(productReq.getName());
        product.setPrice(productReq.getPrice());
        product.setQuantity(productReq.getQuantity());
        product.setDescription(productReq.getDescription());
        product.setCategory(category);

        log.info("Product created successfully");

        // convert list string img -> list entity img
        List<ProductImage> images = new ArrayList<>();
        for (String imgName : productReq.getLstImg()) {
            ProductImage img = new ProductImage();
            img.setImgName(imgName);
            img.setProduct(product);
            images.add(img);
        }
        product.setLstImg(images);

        // 4. Save Product

        Product savedProduct = this.productRepository.save(product);

        ResponseProductDTO res = convertToProductDTO(savedProduct);

        return res;
    }

    public ResponseProductDTO updateProduct(ProductUpdateRequestDTO product) {

        Product productDb = getProductById(product.getId());

        productDb.setName(product.getName());
        productDb.setPrice(product.getPrice());
        productDb.setDescription(product.getDescription());

        int newSold = product.getSold();

        if (newSold < 0) {
            throw new CommonException("Sold quantity must be >= 0");
        }

        // if (newSold > productDb.getQuantity()) {
        // throw new CommonException("Sold quantity cannot be greater than available
        // quantity");
        // }

        productDb.setSold(newSold);
        productDb.setQuantity(product.getQuantity());
        productDb.getLstImg().clear();
        List<ProductImage> newImgs = new ArrayList<>();

        for (String imgName : product.getLstImg()) {
            ProductImage img = new ProductImage();
            img.setImgName(imgName);
            img.setProduct(productDb);
            newImgs.add(img);
        }
        productDb.getLstImg().addAll(newImgs);

        if (product.getProductCate() != null && product.getProductCate().getId() != 0) {
            Category cate = categoryService.getCategoryById(product.getProductCate().getId());
            if (cate != null) {
                productDb.setCategory(cate);
            }

        }

        productRepository.save(productDb);

        return convertToProductDTO(productDb);
    }

    public ResponseProductDTO convertToProductDTO(Product product) {

        // category
        ResponseProductDTO.ProductCate cate = new ResponseProductDTO.ProductCate();
        cate.setId(product.getCategory().getId());
        cate.setName(product.getCategory().getName());

        // convert list image
        List<ResponseProductDTO.ProductImage> lstImg = new ArrayList<>();

        if (product.getLstImg() != null) {
            for (ProductImage img : product.getLstImg()) {
                ResponseProductDTO.ProductImage dtoImg = new ResponseProductDTO.ProductImage();
                dtoImg.setName(img.getImgName());
                lstImg.add(dtoImg);
            }
        }

        // build response
        ResponseProductDTO res = new ResponseProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                lstImg,
                product.getQuantity(),
                product.getSold(),
                product.getDescription(),
                cate,
                product.getCreatedAt(),
                product.getUpdatedAt());

        return res;
    }

    public ResultPaginationDTO search(
            String keyword,
            List<String> categoryNames,
            BigDecimal from,
            BigDecimal to,
            int page,
            int size, String sort) {

        Sort sortObj = Sort.by("updatedAt").descending();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");

            if (parts.length == 2) {
                String field = parts[0];
                String direction = parts[1];

                sortObj = direction.equalsIgnoreCase("desc")
                        ? Sort.by(field).descending()
                        : Sort.by(field).ascending();
            }
        }

        Pageable pageable = PageRequest.of(page - 1, size, sortObj);

        Specification<Product> spec = Specification.allOf(
                ProductSpecification.hasKeyword(keyword),
                ProductSpecification.hasCategory(categoryNames),
                ProductSpecification.priceFrom(from),
                ProductSpecification.priceTo(to));

        Page<Product> pages = productRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pages.getTotalPages());
        meta.setTotalElements(pages.getTotalElements());

        rs.setMeta(meta);

        List<ResponseProductDTO> responseProductDTOs = new ArrayList<>();

        for (Product p : pages.getContent()) {
            ResponseProductDTO.ProductCate productCate = new ResponseProductDTO.ProductCate();
            List<ResponseProductDTO.ProductImage> imgsDto = new ArrayList<>();

            productCate.setId(p.getCategory().getId());
            productCate.setName(p.getCategory().getName());

            List<ProductImage> images = (p.getLstImg() != null) ? p.getLstImg() : new ArrayList<>();

            for (ProductImage i : images) {
                ResponseProductDTO.ProductImage dtoImg = new ResponseProductDTO.ProductImage();
                dtoImg.setName(i.getImgName());
                imgsDto.add(dtoImg);
            }

            ResponseProductDTO res = new ResponseProductDTO(
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    imgsDto,
                    p.getQuantity(),
                    p.getSold(),
                    p.getDescription(),
                    productCate,
                    p.getCreatedAt(),
                    p.getUpdatedAt());

            responseProductDTOs.add(res);
        }

        rs.setResults(responseProductDTOs);
        return rs;
    }

    public void handleDeleteProduct(Long id) {
        Product product = getProductById(id);

        this.productRepository.delete(product);

        Category category = product.getCategory();

        if (category != null) {
            // check cate have product
            long count = this.productRepository.countByCategory_Id(category.getId());

            if (count == 0) {
                this.categoryService.deleteCategory(category.getId());
            }
        }

    }

    public void checkQuantityProductBeforeAddToCart(Product product, int quantity) {

        if (product.getQuantity() < quantity) {
            throw new CommonException(
                    "Product " + product.getId() + " does not have enough stock");
        }
    }

}
