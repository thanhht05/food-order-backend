package com.thanh.foodorder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Cart;
import com.thanh.foodorder.domain.CartDetail;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    List<CartDetail> findByIdIn(List<Long> ids);

    CartDetail findByCartAndProductId(Cart cart, long productId);

}
