package com.thanh.foodorder.feature.product.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.product.dto.ProductRequestDTO;
import com.thanh.foodorder.feature.product.dto.ProductUpdateRequestDTO;
import com.thanh.foodorder.feature.product.dto.ResponseProductDTO;
import com.thanh.foodorder.feature.product.service.ProductService;
import com.thanh.foodorder.feature.upload.service.UploadFileService;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {

        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ResponseProductDTO> handleCreateProduct(@RequestBody ProductRequestDTO p) {

        ResponseProductDTO savedProduct = productService.createProduct(p);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping
    public ResponseEntity<ResponseProductDTO> handleUpdateProduct(
            @RequestBody ProductUpdateRequestDTO product) {

        ResponseProductDTO res = productService.updateProduct(product);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{productId}/status")
    public ResponseEntity<Void> putMethodName(@PathVariable(value = "productId") Long productId) {
        this.productService.updateProductStatus(productId);

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> handleDeletProduct(@PathVariable(value = "id") Long id) {

        this.productService.handleDeleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseProductDTO> handelGetProductById(@PathVariable("id") Long id) {

        Product product = this.productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.productService.convertToProductDTO(product));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getHomePage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) List<String> category,
            @RequestParam(name = "from", required = false) BigDecimal from,
            @RequestParam(name = "to", required = false) BigDecimal to,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sort

    ) throws InterruptedException {

        ResultPaginationDTO result = productService.search(keyword, category, from, to, page, size, sort);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
