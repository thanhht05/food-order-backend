package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.dto.request.ProductRequestDTO;
import com.thanh.foodorder.dto.request.ProductUpdateRequestDTO;
import com.thanh.foodorder.dto.response.product.ResponseProductDTO;
import com.thanh.foodorder.service.ProductService;
import com.thanh.foodorder.service.UploadFileService;

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

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final UploadFileService uploadFileService;
    private final ProductService productService;

    public ProductController(UploadFileService uploadFileService, ProductService productService) {
        this.uploadFileService = uploadFileService;
        this.productService = productService;
    }

    @PostMapping("/products")
    public ResponseEntity<ResponseProductDTO> handleCreateProduct(@RequestBody ProductRequestDTO p) {

        ResponseProductDTO savedProduct = productService.createProduct(p);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping(value = "/products")
    public ResponseEntity<ResponseProductDTO> handleUpdateProduct(
            @RequestBody ProductUpdateRequestDTO product) {

        ResponseProductDTO res = productService.updateProduct(product);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> handleDeletProduct(@PathVariable(value = "id") Long id) {

        this.productService.handleDeleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ResponseProductDTO> handelGetProductById(@PathVariable("id") Long id) {

        Product product = this.productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.productService.convertToProductDTO(product));
    }

    @GetMapping("/products")
    public ResponseEntity<ResultPaginationDTO> getHomePage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) List<String> category,
            @RequestParam(name = "from", required = false) BigDecimal from,
            @RequestParam(name = "to", required = false) BigDecimal to,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sort

    ) throws InterruptedException {
        // Thread.sleep(200); // delay 2 giây

        ResultPaginationDTO result = productService.search(keyword, category, from, to, page, size, sort);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
