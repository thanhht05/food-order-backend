package com.thanh.foodOrder.controller;

import com.thanh.foodOrder.domain.Product;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.domain.respone.ResultPaginationDTO;
import com.thanh.foodOrder.service.CategoryService;
import com.thanh.foodOrder.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

}
