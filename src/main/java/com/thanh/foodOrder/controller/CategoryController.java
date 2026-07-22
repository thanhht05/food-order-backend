package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.Category;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.service.CategoryService;
import com.thanh.foodorder.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    @ApiMessage("Create a category")
    public ResponseEntity<Category> handleCreateCategory(@Valid @RequestBody Category category) {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.createCategory(category));
    }

    @PutMapping("/categories")
    @ApiMessage("Update a category ")
    public ResponseEntity<Category> handleUpdateCategory(@Valid @RequestBody Category category) {

        return ResponseEntity.status(HttpStatus.OK).body(this.categoryService.updateCategory(category));
    }

    @DeleteMapping("/categories/{id}")
    @ApiMessage("Delete a category")
    public ResponseEntity<Void> handleDeleteCate(@PathVariable("id") Long id) {
        this.categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> handleGetCategory(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.categoryService.getCategoryById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<ResultPaginationDTO> handleGetAllCategories(
            @RequestParam(name = "name", required = false) String name,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, value = 5) Pageable pageable) {

        ResultPaginationDTO resultPaginationDTO = this.categoryService.getAllCate(pageable, name);
        return ResponseEntity.status(HttpStatus.OK).body(resultPaginationDTO);
    }

}
