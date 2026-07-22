package com.thanh.foodorder.dto.response.auth;

import com.thanh.foodorder.domain.Role;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseLoginDTO {
    private String accessToken;
    private UserLogin userLogin;

    @Getter
    @Setter
    public static class UserLogin {
        private long id;
        private String email;
        private String fullname;
        private Role role;

    }

    @Getter
    @Setter
    public static class UserGetAccount {
        private UserLogin userLogin;

    }

    @Getter
    @Setter
    public static class UserInsideToken {
        private long id;
        private String fullName;
        private String email;
    }
}
