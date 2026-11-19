package com.thanh.foodorder.feature.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.order.domain.Order;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)

   private Long id;
   @NotBlank(message = "FullName cannot be empty")
   private String fullName;
   @NotBlank(message = "Email cannot be  empty")
   private String email;
   private String password;
   private String phone;
   private String createdBy;
   private String updatedBy;
   private long point;
   @Column(name = "created_at")
   private Instant createdAt;
   private Instant updatedAt;

   @Enumerated(EnumType.STRING)
   @Column(name = "auth_provider")
   private AuthProvider authProvider = AuthProvider.LOCAL;

   @Column(name = "provider_id")
   private String providerId;

   @Column(name = "avatar_url", length = 500)
   private String avatarUrl;

   @Column(name = "is_email_verified")
   private Boolean isEmailVerified = false;

   @Column(columnDefinition = "TEXT")

   String refreshToken;
   @Column(nullable = false)
   private Integer tokenVersion = 0;

   // Phiên đăng nhập đang hoạt động (single active session). null = chưa áp dụng.
   // Tách biệt với tokenVersion (chỉ dùng cho đổi mật khẩu).
   @Column(name = "active_session_id", length = 36)
   private String activeSessionId;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "role_id")
   private Role role;

   @OneToMany(mappedBy = "user")

   private List<Order> orders;

   @OneToOne(mappedBy = "user")

   private Cart cart;

   @PrePersist
   public void handleBeforeCreated() {
      this.createdAt = Instant.now();
      this.createdBy = JwtUtil.getCurrentUserLogin().orElse("");
   }

   @PreUpdate
   public void handleBeforeUpdated() {
      this.updatedBy = JwtUtil.getCurrentUserLogin().orElse("");
      this.updatedAt = Instant.now();
   }
}
