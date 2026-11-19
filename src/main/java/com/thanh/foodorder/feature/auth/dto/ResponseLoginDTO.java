package com.thanh.foodorder.feature.auth.dto;


import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.user.domain.Role;

@Setter
@Getter
public class ResponseLoginDTO {
    private String accessToken;
    private int tokenVersion;
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
