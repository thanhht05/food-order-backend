package com.thanh.foodorder.feature.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.category.domain.Category;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByName(String name);

    Category findByNameIgnoreCase(String name);
}
