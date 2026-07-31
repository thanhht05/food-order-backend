package com.thanh.foodorder.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Category;
import com.thanh.foodorder.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByNameAndCategory(String name, Category category);

    // List<Product> findByCategory(Category cate);

    Page<Product> findByNameContainingIgnoreCaseAndCategory_NameIn(String name, List<String> categoryNames,
            Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategory_NameIn(List<String> categoryNames, Pageable pageable);

    long countByCategory_Id(Long id);

    List<Product> findByNameContainingIgnoreCase(String name);

    @org.springframework.data.jpa.repository.Query("""
                SELECT p
                FROM Product p
                WHERE (:keyword IS NULL
                       OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:category IS NULL
                       OR LOWER(p.category.name) = LOWER(:category))
                  AND (:price IS NULL OR p.price <= :price)
                ORDER BY p.price ASC
            """)
    List<Product> searchForAi(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("price") Double price);

}
