package com.thanh.foodorder.feature.user.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);

    Page<User> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByFullNameAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
