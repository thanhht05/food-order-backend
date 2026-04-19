package com.thanh.foodOrder.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.thanh.foodOrder.domain.Category;
import com.thanh.foodOrder.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByNameAndCategory(String name, Category category);

    // List<Product> findByCategory(Category cate);

    Page<Product> findByNameContainingIgnoreCaseAndCategory_NameIn(String name, List<String> categoryNames,
            Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategory_NameIn(List<String> categoryNames, Pageable pageable);

    long countByCategory_Id(Long id);

}
