package com.thanh.foodorder.feature.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;


@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    List<CartDetail> findByIdIn(List<Long> ids);

    CartDetail findByCartAndProductId(Cart cart, long productId);

    void deleteByCartUserIdAndProductIdIn(Long userId, List<Long> productIds);

}
