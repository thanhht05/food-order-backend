package com.thanh.foodorder.feature.home.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import org.springframework.web.bind.annotation.RequestParam;
import com.thanh.foodorder.feature.category.service.CategoryService;
import com.thanh.foodorder.feature.product.service.ProductService;

@RestController
@RequestMapping("/api/v1")
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

}
