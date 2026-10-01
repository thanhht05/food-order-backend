package com.thanh.foodorder.feature.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    @Query("SELECT cd FROM CartDetail cd JOIN FETCH cd.product WHERE cd.id IN :ids")
    List<CartDetail> findByIdIn(@Param("ids") List<Long> ids);

    CartDetail findByCartAndProductId(Cart cart, long productId);

    void deleteByCartUserIdAndProductIdIn(Long userId, List<Long> productIds);

    @Query("SELECT cd FROM CartDetail cd JOIN FETCH cd.product WHERE cd.cart = :cart")
    List<CartDetail> findByCart(@Param("cart") Cart cart);

    @Query("SELECT DISTINCT cd FROM CartDetail cd " +
            "JOIN FETCH cd.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.lstImg " +
            "WHERE cd.cart = :cart")
    List<CartDetail> findByCartWithDetails(@Param("cart") Cart cart);
}
