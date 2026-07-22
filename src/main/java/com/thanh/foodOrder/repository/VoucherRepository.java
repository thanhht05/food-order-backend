package com.thanh.foodorder.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    boolean existsByCode(String code);

    Page<Voucher> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Optional<Voucher> findByCode(String code);

}