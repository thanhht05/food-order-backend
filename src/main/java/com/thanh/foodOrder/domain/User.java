package com.thanh.foodorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanh.foodorder.util.JwtUtil;

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
   @NotBlank(message = "Password cannot be  empty")
   private String password;
   private String phone;
   private String createdBy;
   private String updatedBy;
   private long point;
   @Column(name = "created_at")
   private Instant createdAt;
   private Instant updatedAt;
   String refreshToken;
   @Column(nullable = false)
   private Integer tokenVersion = 0;

   @ManyToOne()
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
