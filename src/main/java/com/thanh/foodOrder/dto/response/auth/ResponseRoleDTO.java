package com.thanh.foodorder.dto.response.auth;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseRoleDTO {
    private long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;
    private String createdBy;
}
