package com.thanh.foodorder.feature.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.auth.dto.RequestLoginDTO;
import com.thanh.foodorder.feature.auth.dto.ResponseLoginDTO;
import com.thanh.foodorder.feature.user.domain.Role;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.dto.ChangePasswordRequest;
import com.thanh.foodorder.feature.user.dto.ResponseUserDTO;
import com.thanh.foodorder.feature.user.service.UserService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthenticationManager authenticationManager;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtUtil jwtUtil;

        private User sampleUser;
        private Role sampleRole;

        @BeforeEach
        void setUp() {
                sampleRole = new Role();
                sampleRole.setId(1L);
                sampleRole.setName("ROLE_USER");

                sampleUser = new User();
                sampleUser.setId(10L);
                sampleUser.setEmail("user@example.com");
                sampleUser.setFullName("Nguyen Van A");
                sampleUser.setTokenVersion(1);
                sampleUser.setRole(sampleRole);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        // ==========================================
        // 1. POST /api/v1/auth/register
        // ==========================================
        @Nested
        @DisplayName("POST /api/v1/auth/register Tests")
        class RegisterTests {

                @Test
                @DisplayName("POST /api/v1/auth/register - Success")
                void testRegister_Success() throws Exception {
                        ResponseUserDTO responseUserDTO = new ResponseUserDTO();
                        responseUserDTO.setId(10L);
                        responseUserDTO.setEmail("user@example.com");
                        responseUserDTO.setFullName("Nguyen Van A");

                        when(userService.createUser(any(User.class))).thenReturn(responseUserDTO);

                        mockMvc.perform(post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sampleUser)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.id").value(10))
                                        .andExpect(jsonPath("$.data.email").value("user@example.com"))
                                        .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"));

                        verify(userService, times(1)).createUser(any(User.class));
                }

                @Test
                @DisplayName("POST /api/v1/auth/register - Failure when email already exists")
                void testRegister_EmailAlreadyExists() throws Exception {
                        when(userService.createUser(any(User.class)))
                                        .thenThrow(new CommonException("Email đã tồn tại!"));

                        mockMvc.perform(post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sampleUser)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Email đã tồn tại!"));

                        verify(userService, times(1)).createUser(any(User.class));
                }
        }

        // ==========================================
        // 2. POST /api/v1/auth/login
        // ==========================================
        @Nested
        @DisplayName("POST /api/v1/auth/login Tests")
        class LoginTests {

                @Test
                @DisplayName("POST /api/v1/auth/login - Success with valid credentials and sets cookie")
                void testLogin_Success() throws Exception {
                        RequestLoginDTO loginDTO = new RequestLoginDTO();
                        loginDTO.setUsername("user@example.com");
                        loginDTO.setPassword("Password123@");

                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                        "user@example.com", "Password123@", Collections.emptyList());

                        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                        .thenReturn(auth);
                        when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);
                        when(jwtUtil.generateToken(eq(sampleUser), any(ResponseLoginDTO.class)))
                                        .thenReturn("mock-access-token-123");
                        when(jwtUtil.generateRefreshToken(eq(sampleUser), any(ResponseLoginDTO.class)))
                                        .thenReturn("mock-refresh-token-456");
                        doNothing().when(userService).updateUserRefreshToken("user@example.com",
                                        "mock-refresh-token-456");

                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginDTO)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.statusCode").value(200))
                                        .andExpect(jsonPath("$.message").value("Login "))
                                        .andExpect(jsonPath("$.data.accessToken").value("mock-access-token-123"))
                                        .andExpect(jsonPath("$.data.userLogin.id").value(10))
                                        .andExpect(jsonPath("$.data.userLogin.email").value("user@example.com"))
                                        .andExpect(jsonPath("$.data.userLogin.fullname").value("Nguyen Van A"))
                                        .andExpect(cookie().value("refreshToken", "mock-refresh-token-456"))
                                        .andExpect(cookie().httpOnly("refreshToken", true))
                                        .andExpect(cookie().secure("refreshToken", true))
                                        .andExpect(cookie().path("refreshToken", "/"))
                                        .andExpect(cookie().maxAge("refreshToken", 2 * 24 * 60 * 60));

                        verify(authenticationManager, times(1))
                                        .authenticate(any(UsernamePasswordAuthenticationToken.class));
                        verify(userService, times(1)).getUserByEmail("user@example.com");
                        verify(userService, times(1)).updateUserRefreshToken("user@example.com",
                                        "mock-refresh-token-456");
                }

                @Test
                @DisplayName("POST /api/v1/auth/login - Failure with incorrect username or password")
                void testLogin_BadCredentials() throws Exception {
                        RequestLoginDTO loginDTO = new RequestLoginDTO();
                        loginDTO.setUsername("wrong@example.com");
                        loginDTO.setPassword("WrongPassword");

                        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                        .thenThrow(new BadCredentialsException("Bad credentials"));

                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginDTO)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Username or password incorrect"));

                        verify(authenticationManager, times(1))
                                        .authenticate(any(UsernamePasswordAuthenticationToken.class));
                }

                @Test
                @DisplayName("POST /api/v1/auth/login - Failure when user not found after authentication")
                void testLogin_UserNotFound() throws Exception {
                        RequestLoginDTO loginDTO = new RequestLoginDTO();
                        loginDTO.setUsername("notfound@example.com");
                        loginDTO.setPassword("Password123@");

                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                        "notfound@example.com", "Password123@", Collections.emptyList());

                        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                        .thenReturn(auth);
                        when(userService.getUserByEmail("notfound@example.com"))
                                        .thenThrow(new CommonException("User không tồn tại"));

                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginDTO)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("User không tồn tại"));
                }
        }

        // ==========================================
        // 3. GET /api/v1/auth/me
        // ==========================================
        @Nested
        @DisplayName("GET /api/v1/auth/me Tests")
        class GetUserAccountTests {

                @Test
                @DisplayName("GET /api/v1/auth/me - Success when user is authenticated")
                void testGetUserAccount_Success() throws Exception {
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                        "user@example.com", null, Collections.emptyList());
                        context.setAuthentication(auth);
                        SecurityContextHolder.setContext(context);

                        when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);

                        mockMvc.perform(get("/api/v1/auth/me"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.statusCode").value(200))
                                        .andExpect(jsonPath("$.message").value("Get user account"))
                                        .andExpect(jsonPath("$.data.userLogin.id").value(10))
                                        .andExpect(jsonPath("$.data.userLogin.email").value("user@example.com"))
                                        .andExpect(jsonPath("$.data.userLogin.fullname").value("Nguyen Van A"))
                                        .andExpect(jsonPath("$.data.userLogin.role.name").value("ROLE_USER"));

                        verify(userService, times(1)).getUserByEmail("user@example.com");
                }

                @Test
                @DisplayName("GET /api/v1/auth/me - Failure when user not found in database")
                void testGetUserAccount_NotFound() throws Exception {
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                        "unknown@example.com", null, Collections.emptyList());
                        context.setAuthentication(auth);
                        SecurityContextHolder.setContext(context);

                        when(userService.getUserByEmail("unknown@example.com"))
                                        .thenThrow(new CommonException("User không tồn tại"));

                        mockMvc.perform(get("/api/v1/auth/me"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("User không tồn tại"));
                }
        }

        // ==========================================
        // 4. GET /api/v1/auth/refreshToken
        // ==========================================
        @Nested
        @DisplayName("GET /api/v1/auth/refreshToken Tests")
        class RefreshTokenTests {

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Success with valid refreshToken cookie")
                void testRefreshToken_Success() throws Exception {
                        Cookie requestCookie = new Cookie("refreshToken", "valid-refresh-token-value");

                        when(jwtUtil.extractUsername("valid-refresh-token-value")).thenReturn("user@example.com");
                        when(userService.fetchUserByEmailAndRefreshToken("user@example.com",
                                        "valid-refresh-token-value"))
                                        .thenReturn(sampleUser);
                        when(jwtUtil.validRefreshToken("valid-refresh-token-value", 1)).thenReturn(true);
                        when(jwtUtil.generateToken(eq(sampleUser), any(ResponseLoginDTO.class)))
                                        .thenReturn("new-access-token-789");
                        when(jwtUtil.generateRefreshToken(eq(sampleUser), any(ResponseLoginDTO.class)))
                                        .thenReturn("new-refresh-token-789");
                        doNothing().when(userService).updateUserRefreshToken("user@example.com",
                                        "new-refresh-token-789");

                        mockMvc.perform(get("/api/v1/auth/refreshToken")
                                        .cookie(requestCookie))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.statusCode").value(200))
                                        .andExpect(jsonPath("$.data.accessToken").value("new-access-token-789"))
                                        .andExpect(jsonPath("$.data.userLogin.email").value("user@example.com"))
                                        .andExpect(cookie().value("refreshToken", "new-refresh-token-789"))
                                        .andExpect(cookie().httpOnly("refreshToken", true))
                                        .andExpect(cookie().secure("refreshToken", true))
                                        .andExpect(cookie().path("refreshToken", "/"))
                                        .andExpect(cookie().maxAge("refreshToken", 2 * 24 * 60 * 60));

                        verify(jwtUtil, times(1)).extractUsername("valid-refresh-token-value");
                        verify(userService, times(1)).fetchUserByEmailAndRefreshToken("user@example.com",
                                        "valid-refresh-token-value");
                        verify(jwtUtil, times(1)).validRefreshToken("valid-refresh-token-value", 1);
                        verify(userService, times(1)).updateUserRefreshToken("user@example.com",
                                        "new-refresh-token-789");
                }

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Failure when cookie is missing")
                void testRefreshToken_MissingCookie() throws Exception {
                        mockMvc.perform(get("/api/v1/auth/refreshToken"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Cookie does not exist"));
                }

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Failure when cookie is blank")
                void testRefreshToken_BlankCookie() throws Exception {
                        Cookie blankCookie = new Cookie("refreshToken", "   ");

                        mockMvc.perform(get("/api/v1/auth/refreshToken")
                                        .cookie(blankCookie))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Cookie does not exist"));
                }

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Failure when token is invalid (cannot extract username)")
                void testRefreshToken_InvalidTokenExtractUsernameNull() throws Exception {
                        Cookie requestCookie = new Cookie("refreshToken", "invalid-token");
                        when(jwtUtil.extractUsername("invalid-token")).thenReturn(null);

                        mockMvc.perform(get("/api/v1/auth/refreshToken")
                                        .cookie(requestCookie))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Invalid refresh token"));
                }

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Failure when user not found or token revoked")
                void testRefreshToken_UserNotFoundOrRevoked() throws Exception {
                        Cookie requestCookie = new Cookie("refreshToken", "revoked-token");
                        when(jwtUtil.extractUsername("revoked-token")).thenReturn("user@example.com");
                        when(userService.fetchUserByEmailAndRefreshToken("user@example.com", "revoked-token"))
                                        .thenReturn(null);

                        mockMvc.perform(get("/api/v1/auth/refreshToken")
                                        .cookie(requestCookie))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message")
                                                        .value("User not found or refresh token revoked"));
                }

                @Test
                @DisplayName("GET /api/v1/auth/refreshToken - Failure when refresh token is expired or version mismatch")
                void testRefreshToken_TokenInvalidOrExpired() throws Exception {
                        Cookie requestCookie = new Cookie("refreshToken", "expired-token");
                        when(jwtUtil.extractUsername("expired-token")).thenReturn("user@example.com");
                        when(userService.fetchUserByEmailAndRefreshToken("user@example.com", "expired-token"))
                                        .thenReturn(sampleUser);
                        when(jwtUtil.validRefreshToken("expired-token", 1)).thenReturn(false);

                        mockMvc.perform(get("/api/v1/auth/refreshToken")
                                        .cookie(requestCookie))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Refresh token invalid or expired"));
                }
        }

        // ==========================================
        // 5. POST /api/v1/auth/changePassword
        // ==========================================
        @Nested
        @DisplayName("POST /api/v1/auth/changePassword Tests")
        class ChangePasswordTests {

                @Test
                @DisplayName("POST /api/v1/auth/changePassword - Success")
                void testChangePassword_Success() throws Exception {
                        ChangePasswordRequest req = new ChangePasswordRequest();
                        req.setOldPassword("OldPass@123");
                        req.setNewPassword("NewPass@123");
                        req.setConfirmPassword("NewPass@123");

                        ResponseLoginDTO responseDTO = new ResponseLoginDTO();
                        responseDTO.setAccessToken("token-after-password-change");

                        when(userService.userChangePassword(any(ChangePasswordRequest.class)))
                                        .thenReturn(responseDTO);

                        mockMvc.perform(post("/api/v1/auth/changePassword")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.statusCode").value(200))
                                        .andExpect(jsonPath("$.data.accessToken").value("token-after-password-change"));

                        verify(userService, times(1)).userChangePassword(any(ChangePasswordRequest.class));
                }

                @Test
                @DisplayName("POST /api/v1/auth/changePassword - Failure when old password does not match")
                void testChangePassword_IncorrectOldPassword() throws Exception {
                        ChangePasswordRequest req = new ChangePasswordRequest();
                        req.setOldPassword("WrongOldPass");
                        req.setNewPassword("NewPass@123");
                        req.setConfirmPassword("NewPass@123");

                        when(userService.userChangePassword(any(ChangePasswordRequest.class)))
                                        .thenThrow(new CommonException("Mật khẩu cũ không chính xác"));

                        mockMvc.perform(post("/api/v1/auth/changePassword")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.statusCode").value(400))
                                        .andExpect(jsonPath("$.message").value("Mật khẩu cũ không chính xác"));

                        verify(userService, times(1)).userChangePassword(any(ChangePasswordRequest.class));
                }
        }
}
