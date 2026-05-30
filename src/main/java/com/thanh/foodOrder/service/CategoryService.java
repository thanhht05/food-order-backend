package com.thanh.foodOrder.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.thanh.foodOrder.domain.Category;
import com.thanh.foodOrder.domain.respone.ResultPaginationDTO;
import com.thanh.foodOrder.repository.CategoryRepository;
import com.thanh.foodOrder.specification.CategorySpecification;
import com.thanh.foodOrder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public boolean checkExistsByName(String name) {
        return this.categoryRepository.existsByName(name);
    }

    public Category createCategory(Category category) {
        if (checkExistsByName(category.getName())) {
            log.warn("Name {} already exists", category.getName());
            throw new CommonException("Name " + category.getName() + " already exists");
        }

        log.info("Category created successfully ");
        return this.categoryRepository.save(category);

    }

    public Category getCategoryById(Long id) {
        return this.categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("Category with id: {} not found", id);
            return new CommonException("Category with id " + id + " not found");

        });
    }

    public Category getCategoryByName(String name) {
        Category category = this.categoryRepository.findByNameIgnoreCase(name);
        if (category == null) {
            log.warn("Category with name: {} not found", name);
            throw new CommonException("Category with name " + name + " not found");
        }
        return category;
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        this.categoryRepository.delete(category);
    }

    public Category updateCategory(Category category) {
        log.info("Updating category with id: {}", category.getId());

        Category existingCategory = getCategoryById(category.getId());

        // check name if has change
        if (!existingCategory.getName().equals(category.getName())
                && checkExistsByName(category.getName())) {
            log.warn("Name {} already exists", category.getName());
            throw new CommonException("Name " + category.getName() + " already exists");
        }

        existingCategory.setName(category.getName());

        log.info("Category updated successfully");
        return categoryRepository.save(existingCategory);
    }

    public ResultPaginationDTO getAllCate(Pageable pageable, String name) {
        Specification<Category> spec = Specification.allOf(
                name != null ? CategorySpecification.hasName(name) : null);
        Page<Category> categories = this.categoryRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(categories.getTotalPages());
        meta.setTotalElements(categories.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResults(categories.getContent());

        return resultPaginationDTO;
    }

}
