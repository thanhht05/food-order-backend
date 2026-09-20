package com.thanh.foodorder.feature.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.cart.domain.Cart;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(long id);
}
