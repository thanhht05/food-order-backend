package com.thanh.foodOrder.domain;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanh.foodOrder.enums.TableStatus;
import com.thanh.foodOrder.util.JwtUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "restaurant_table")
@AllArgsConstructor
@NoArgsConstructor
public class BookingTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private TableStatus tableStatus;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

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

    @JsonIgnore
    @OneToMany(mappedBy = "bookingTable")
    private List<Order> orders;

}
