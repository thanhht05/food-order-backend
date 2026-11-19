package com.thanh.foodorder.feature.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.user.domain.Role;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.dto.ResponseUserDTO;
import com.thanh.foodorder.feature.user.service.RoleService;
import com.thanh.foodorder.feature.user.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RoleService roleService;

    private User user;
    private ResponseUserDTO responseUserDTO;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setEmail("thanh@gmail.com");
        user.setFullName("Thanh Nguyen");
        user.setPassword("123456");
        user.setPhone("0987654321");
        user.setRole(role);

        ResponseUserDTO.RoleUser roleUser = new ResponseUserDTO.RoleUser();
        roleUser.setId(role.getId());
        roleUser.setName(role.getName());

        responseUserDTO = new ResponseUserDTO();
        responseUserDTO.setId(1L);
        responseUserDTO.setEmail("thanh@gmail.com");
        responseUserDTO.setFullName("Thanh Nguyen");
        responseUserDTO.setPhone("0987654321");
        responseUserDTO.setRoleUser(roleUser);
    }

    @Nested
    @DisplayName("POST /api/v1/users Tests")
    class CreateUserTests {

        @Test
        @DisplayName("POST /api/v1/users - Success")
        void handleCreateUser_Success() throws Exception {
            when(userService.createUser(any(User.class))).thenReturn(responseUserDTO);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("thanh@gmail.com"))
                    .andExpect(jsonPath("$.data.fullName").value("Thanh Nguyen"));

            verify(userService, times(1)).createUser(any(User.class));
        }

        @Test
        @DisplayName("POST /api/v1/users - Validation Error when email is blank")
        void handleCreateUser_WhenEmailIsBlank_ShouldReturnBadRequest() throws Exception {
            user.setEmail("");

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("POST /api/v1/users - Validation Error when fullName is blank")
        void handleCreateUser_WhenFullNameIsBlank_ShouldReturnBadRequest() throws Exception {
            user.setFullName("");

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("POST /api/v1/users - Failure when email already exists")
        void handleCreateUser_EmailExists_ShouldReturnBadRequest() throws Exception {
            when(userService.createUser(any(User.class)))
                    .thenThrow(new CommonException("Email already exists"));

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email already exists"));

            verify(userService, times(1)).createUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("PUT /api/v1/users - Success")
        void handleUpdateUser_Success() throws Exception {
            when(userService.updateUser(any(User.class))).thenReturn(responseUserDTO);

            mockMvc.perform(put("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.fullName").value("Thanh Nguyen"));

            verify(userService, times(2)).updateUser(any(User.class));
        }

        @Test
        @DisplayName("PUT /api/v1/users - Failure when user not found")
        void handleUpdateUser_NotFound_ShouldReturnBadRequest() throws Exception {
            when(userService.updateUser(any(User.class)))
                    .thenThrow(new CommonException("User with id 99 not found"));

            mockMvc.perform(put("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User with id 99 not found"));

            verify(userService, times(1)).updateUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("DELETE /api/v1/users/{id} - Success")
        void handleDeleteUser_Success() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/v1/users/1"))
                    .andExpect(status().isOk());

            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @DisplayName("DELETE /api/v1/users/{id} - Failure when user not found")
        void handleDeleteUser_NotFound_ShouldReturnBadRequest() throws Exception {
            doThrow(new CommonException("User with id 99 not found"))
                    .when(userService).deleteUser(99L);

            mockMvc.perform(delete("/api/v1/users/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User with id 99 not found"));

            verify(userService, times(1)).deleteUser(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("GET /api/v1/users/{id} - Success")
        void handleGetUserById_Success() throws Exception {
            when(userService.getUserById(1L)).thenReturn(user);
            when(userService.convertUserToResUserDTO(user)).thenReturn(responseUserDTO);

            mockMvc.perform(get("/api/v1/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("thanh@gmail.com"));

            verify(userService, times(1)).getUserById(1L);
            verify(userService, times(1)).convertUserToResUserDTO(user);
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} - Failure when user not found")
        void handleGetUserById_NotFound_ShouldReturnBadRequest() throws Exception {
            when(userService.getUserById(99L))
                    .thenThrow(new CommonException("User with id 99 not found"));

            mockMvc.perform(get("/api/v1/users/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User with id 99 not found"));

            verify(userService, times(1)).getUserById(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users Tests")
    class GetAllUserTests {

        @Test
        @DisplayName("GET /api/v1/users - Success with filter parameters")
        void getAllUser_WithFilters_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(2);
            meta.setPageSize(5);
            meta.setPages(3);
            meta.setTotalElements(15L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseUserDTO));

            when(userService.getAllUser(eq(2), eq(5), eq("Thanh"), eq("thanh@gmail.com"), eq("id,desc")))
                    .thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/users")
                    .param("fullName", "Thanh")
                    .param("email", "thanh@gmail.com")
                    .param("page", "2")
                    .param("size", "5")
                    .param("sort", "id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(2))
                    .andExpect(jsonPath("$.data.meta.pageSize").value(5))
                    .andExpect(jsonPath("$.data.meta.totalElements").value(15))
                    .andExpect(jsonPath("$.data.results[0].email").value("thanh@gmail.com"));

            verify(userService, times(1)).getAllUser(eq(2), eq(5), eq("Thanh"), eq("thanh@gmail.com"), eq("id,desc"));
        }

        @Test
        @DisplayName("GET /api/v1/users - Success with default parameters")
        void getAllUser_WithDefaultParams_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(1);
            meta.setPageSize(5);
            meta.setPages(1);
            meta.setTotalElements(1L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseUserDTO));

            when(userService.getAllUser(eq(1), eq(5), eq(null), eq(null), eq(null)))
                    .thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(1))
                    .andExpect(jsonPath("$.data.meta.pageSize").value(5))
                    .andExpect(jsonPath("$.data.results[0].email").value("thanh@gmail.com"));

            verify(userService, times(1)).getAllUser(eq(1), eq(5), eq(null), eq(null), eq(null));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/bulk Tests")
    class CreateUserBulkTests {

        @Test
        @DisplayName("POST /api/v1/users/bulk - Success")
        void createUserBulk_Success() throws Exception {
            Map<String, Object> bulkResult = new HashMap<>();
            bulkResult.put("count", 2);
            bulkResult.put("message", "Created 2 users successfully");

            when(userService.createUserBulk(any())).thenReturn(bulkResult);

            mockMvc.perform(post("/api/v1/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(user))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(2));

            verify(userService, times(1)).createUserBulk(any());
        }
    }
}
