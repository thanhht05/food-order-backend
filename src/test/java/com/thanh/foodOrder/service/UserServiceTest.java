package com.thanh.foodorder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thanh.foodorder.domain.Role;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.dto.request.ChangePasswordRequest;
import com.thanh.foodorder.dto.response.auth.ResponseLoginDTO;
import com.thanh.foodorder.dto.response.user.ResponseUserDTO;
import com.thanh.foodorder.repository.UserRepository;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.exception.CommonException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

        @Mock
        private UserRepository userRepository;
        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private RoleService roleService;

        @InjectMocks
        private UserService userService;
        private User user;

        @BeforeEach
        void setUp() {
                Role role = new Role();
                role.setId(1L);
                role.setName("USER");

                user = new User();
                user.setId(1L);
                user.setFullName("test");
                user.setEmail("test@gmail.com");
                user.setPassword("123456");
                user.setRole(role);
        }

        @Test
        void shouldCreateUserSuccessfully() {

                // simulate email not exists
                when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);

                // Giả lập mã hóa password
                // encode("123456") -> "hashed_password": Không cần encoder thật.
                when(passwordEncoder.encode("123456"))
                                .thenReturn("hashed_password");

                // Giả lập lưu vào Database
                User savedUser = new User();
                savedUser.setId(1L);
                savedUser.setEmail("test@gmail.com");
                savedUser.setPassword("hashed_password");
                savedUser.setRole(user.getRole());

                when(userRepository.save(user))
                                .thenReturn(savedUser);

                // Act
                ResponseUserDTO result = userService.createUser(user);

                // Assert
                assertEquals(1L, result.getId());
                assertEquals("test@gmail.com", result.getEmail());

                verify(userRepository).save(any(User.class));

        }

        @Test
        void createUser_EmailExists_ShouldThrowException() {

                // simulate email not exists

                when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

                // Act + Assert
                assertThrows(
                                RuntimeException.class,
                                () -> userService.createUser(user));
        }

        @Test
        void shouldGetUserSuccessfully() {

                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

                User userDb = userService.getUserById(user.getId());

                assertEquals("test@gmail.com", userDb.getEmail());
                assertEquals("test", userDb.getFullName());

        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {

                when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(CommonException.class, () -> userService.getUserById(user.getId()));
        }

        @Test
        void shouldGetAllUsersSuccessfully() {

                List<User> userList = List.of(user);
                Pageable pageable = PageRequest.of(0, 10);

                Page<User> page = new PageImpl<>(
                                userList,
                                pageable,
                                1);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(page);

                // Act
                ResultPaginationDTO result = userService.getAllUser(1, 10, null, null, null);

                // Assert - Meta
                assertEquals(1, result.getMeta().getPage());
                assertEquals(10, result.getMeta().getPageSize());
                assertEquals(1, result.getMeta().getPages());
                assertEquals(1, result.getMeta().getTotalElements());

                // Assert - Results
                List<ResponseUserDTO> results = (List<ResponseUserDTO>) result.getResults();

                assertEquals(1, results.size());
                assertEquals("test", results.get(0).getFullName());

                // Verify repository
                // Kiểm tra xem userRepository.findAll() có được gọi hay chưa.
                verify(userRepository).findAll(any(Pageable.class));

        }

        @Test
        void shouldUpdateUserSuccessfully() {
                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
                Role role = new Role();
                role.setId(2L);
                role.setName("ADMIN");

                User updateUser = new User();
                updateUser.setId(1L);
                updateUser.setRole(role);
                updateUser.setFullName("updateTest");
                updateUser.setPhone("updatePhoneTest");

                when(userRepository.save(user))
                                .thenReturn(updateUser);

                ResponseUserDTO result = userService.updateUser(user);

                assertEquals("updateTest", result.getFullName());
                assertEquals("updatePhoneTest", result.getPhone());
                assertEquals("ADMIN", result.getRoleUser().getName());

        }

        @Test
        void shouldGetUsersByEmail() {

                List<User> userList = List.of(user);
                Pageable pageable = PageRequest.of(0, 10);

                Page<User> page = new PageImpl<>(
                                userList,
                                pageable,
                                1);
                when(userRepository.findByEmailContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

                ResultPaginationDTO result = userService.getAllUser(1, 10, null, "gmail.com", null);

                assertEquals(1, result.getMeta().getPage());
                assertEquals(10, result.getMeta().getPageSize());
                assertEquals(1, result.getMeta().getPages());
                assertEquals(1, result.getMeta().getTotalElements());

                List<ResponseUserDTO> results = (List<ResponseUserDTO>) result.getResults();
                assertEquals(1, results.size());
                assertEquals("test", results.get(0).getFullName());

                verify(userRepository).findByEmailContainingIgnoreCase(anyString(), any(Pageable.class));

        }

        @Test
        void shouldGetUsersByFullNameAndEmail() {
                List<User> userList = List.of(user);
                Pageable pageable = PageRequest.of(0, 10);

                Page<User> page = new PageImpl<>(
                                userList,
                                pageable,
                                1);

                when(userRepository.findByFullNameAndEmailContainingIgnoreCase(anyString(), anyString(),
                                any(Pageable.class)))
                                .thenReturn(page);

                ResultPaginationDTO result = userService.getAllUser(1, 10, "test", "gmail.com", null);

                assertEquals(1, result.getMeta().getPage());
                assertEquals(10, result.getMeta().getPageSize());
                assertEquals(1, result.getMeta().getPages());
                assertEquals(1, result.getMeta().getTotalElements());

                List<ResponseUserDTO> results = (List<ResponseUserDTO>) result.getResults();
                assertEquals(1, results.size());
                assertEquals("test", results.get(0).getFullName());

                verify(userRepository).findByFullNameAndEmailContainingIgnoreCase(anyString(), anyString(),
                                any(Pageable.class));

        }

        @Test
        void shouldThrowException_WhenOldPasswordIncorrect() {
                ChangePasswordRequest req = new ChangePasswordRequest();
                req.setOldPassword("wrong");
                req.setNewPassword("new123");
                req.setConfirmPassword("new123");

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {

                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userRepository.findByEmail("test@gmail.com"))
                                        .thenReturn(Optional.of(user));

                        when(passwordEncoder.matches("wrong", user.getPassword()))
                                        .thenReturn(false);

                        assertThrows(
                                        CommonException.class,
                                        () -> userService.userChangePassword(req));
                }
        }

        @Test
        void userChangePassword_confirmPasswordNotMatch_throwException() {
                ChangePasswordRequest req = new ChangePasswordRequest();
                req.setOldPassword("wrong");
                req.setNewPassword("new123");
                req.setConfirmPassword("new123a");

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userRepository.findByEmail("test@gmail.com"))
                                        .thenReturn(Optional.of(user));

                        when(passwordEncoder.matches("wrong", user.getPassword()))
                                        .thenReturn(true);

                        assertThrows(
                                        CommonException.class,
                                        () -> userService.userChangePassword(req));
                }
        }

        @Test
        void userChangePasswordSuccessfully() {
                ChangePasswordRequest req = new ChangePasswordRequest();
                req.setOldPassword("wrong");
                req.setNewPassword("new123");
                req.setConfirmPassword("new123");

                user.setTokenVersion(1);
                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userRepository.findByEmail("test@gmail.com"))
                                        .thenReturn(Optional.of(user));

                        when(passwordEncoder.matches("wrong", user.getPassword()))
                                        .thenReturn(true);
                        when(passwordEncoder.encode(req.getNewPassword()))
                                        .thenReturn("hashed_password");

                        ResponseLoginDTO res = userService.userChangePassword(req);

                        assertEquals(2, res.getTokenVersion());
                        assertEquals("test", res.getUserLogin().getFullname());
                        verify(userRepository).save(user);

                }
        }
}
