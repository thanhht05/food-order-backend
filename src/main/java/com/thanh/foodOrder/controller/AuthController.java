package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.request.ChangePasswordRequest;
import com.thanh.foodorder.dto.request.RequestLoginDTO;
import com.thanh.foodorder.dto.request.RequestRegisterDTO;
import com.thanh.foodorder.dto.response.auth.ResponseLoginDTO;
import com.thanh.foodorder.dto.response.user.ResponseUserDTO;
import com.thanh.foodorder.service.UserService;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.annotation.ApiMessage;
import com.thanh.foodorder.util.exception.CommonException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseUserDTO> handleRegister(@RequestBody User user) {

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.createUser(user));
    }

    @PostMapping("/auth/login")
    @ApiMessage("Login ")
    public ResponseEntity<ResponseLoginDTO> handleLogin(@RequestBody RequestLoginDTO loginDTO,
            HttpServletResponse response) {

        // pass username and password
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
                loginDTO.getPassword());

        // authenticate=>loadUserByUsername
        Authentication authentication = authenticationManager.authenticate(token);

        // // set authentication in SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userService.getUserByEmail(loginDTO.getUsername());

        // generate accessToken
        ResponseLoginDTO res = new ResponseLoginDTO();
        ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
        userLogin.setEmail(user.getEmail());
        userLogin.setFullname(user.getFullName());
        userLogin.setId(user.getId());
        userLogin.setRole(user.getRole());
        res.setUserLogin(userLogin);
        String accessToken = jwtUtil.generateToken(user, res);

        // generate refreshToken
        String refreshToken = jwtUtil.generateRefreshToken(user, res);

        // update user with refreshToken
        this.userService.updateUserRefreshToken(loginDTO.getUsername(), refreshToken);

        res.setAccessToken(accessToken);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(2 * 24 * 60 * 60);// 2days (s)
        cookie.setPath("/");
        cookie.setSecure(true);
        response.addCookie(cookie);
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/auth/me")
    @ApiMessage("Get user account")
    public ResponseEntity<ResponseLoginDTO.UserGetAccount> handleGetUserAccount() {
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User user = this.userService.getUserByEmail(email);
        ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
        ResponseLoginDTO.UserGetAccount userGetAccount = new ResponseLoginDTO.UserGetAccount();
        userLogin.setId(user.getId());
        userLogin.setEmail(email);
        userLogin.setFullname(user.getFullName());
        userLogin.setRole(user.getRole());
        userGetAccount.setUserLogin(userLogin);
        return ResponseEntity.ok().body(userGetAccount);

    }

    @GetMapping("auth/refreshToken")
    public ResponseEntity<ResponseLoginDTO> handleRefreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "defaultToken") String refreshToken,
            HttpServletResponse response) {

        // 1. Kiểm tra cookie hợp lệ trước tiên
        if (refreshToken == null || refreshToken.isBlank() || "defaultToken".equals(refreshToken)) {
            throw new CommonException("Cookie does not exist");
        }

        // 2. Trích xuất email (cần đảm bảo jwtUtil xử lý parse an toàn)
        String email = jwtUtil.extractUsername(refreshToken);
        if (email == null) {
            throw new CommonException("Invalid refresh token");
        }

        // 3. Tìm user trong DB và kiểm tra null
        User userDb = this.userService.fetchUserByEmailAndRefreshToken(email, refreshToken);
        if (userDb == null) {
            throw new CommonException("User not found or refresh token revoked");
        }

        // 4. Validate token version & hạn sử dụng
        if (!jwtUtil.validRefreshToken(refreshToken, userDb.getTokenVersion())) {
            throw new CommonException("Refresh token invalid or expired");
        }

        // 5. Tạo response & cập nhật token mới
        ResponseLoginDTO res = new ResponseLoginDTO();
        ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
        userLogin.setEmail(userDb.getEmail());
        userLogin.setId(userDb.getId());
        userLogin.setFullname(userDb.getFullName());
        userLogin.setRole(userDb.getRole());
        res.setUserLogin(userLogin);

        String accessToken = jwtUtil.generateToken(userDb, res);
        res.setAccessToken(accessToken);

        String newRefreshToken = jwtUtil.generateRefreshToken(userDb, res);
        this.userService.updateUserRefreshToken(email, newRefreshToken);

        // 6. Set lại cookie mới
        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(2 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok().body(res);
    }

    // api in CustomLogoutSuccessHandler
    // @PostMapping("auth/logout")
    // public ResponseEntity<?> logout(HttpServletResponse response) {

    // return ResponseEntity.ok("Logged out");
    // }

    @PostMapping("/auth/changePassword")
    public ResponseEntity<ResponseLoginDTO> postMethodName(@RequestBody ChangePasswordRequest req) {

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.userChangePassword(req));
    }

}
