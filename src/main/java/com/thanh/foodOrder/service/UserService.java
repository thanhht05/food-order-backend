package com.thanh.foodOrder.service;

import com.thanh.foodOrder.domain.Role;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.domain.respone.ResultPaginationDTO;
import com.thanh.foodOrder.dtos.response.user.ResponseUserDTO;
import com.thanh.foodOrder.repository.UserRepository;
import com.thanh.foodOrder.specification.UserSpecification;
import com.thanh.foodOrder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return this.userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id: {} not found", id);
            return new CommonException("User with id " + id + " not found");
        });
    }

    public ResponseUserDTO createUser(User user) {
        log.info("Creating user with email: {}", user.getEmail());

        if (user.getRole() != null) {
            Role role = this.roleService.getRoleById(user.getRole().getId());
            user.setRole(role);
        }

        if (checkExistsByEmail(user.getEmail())) {
            log.warn("Email {} already exists", user.getEmail());
            throw new CommonException("Email " + user.getEmail() + " already exists");
        }
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return convertUserToResUserDTO(savedUser);
    }

    public ResponseUserDTO convertUserToResUserDTO(User user) {
        ResponseUserDTO userDTO = new ResponseUserDTO();
        ResponseUserDTO.RoleUser roleUser = new ResponseUserDTO.RoleUser();
        userDTO.setId(user.getId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPoint(user.getPoint());
        userDTO.setPhone(user.getPhone());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());

        roleUser.setId(user.getRole().getId());
        roleUser.setName(user.getRole().getName());
        userDTO.setRoleUser(roleUser);
        return userDTO;
    }

    public boolean checkExistsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResponseUserDTO updateUser(User user) {
        log.info("Updating user with id: {}", user.getId());

        User existingUser = this.getUserById(user.getId());
        if (user.getRole() != null) {
            Role role = this.roleService.getRoleById(user.getRole().getId());
            existingUser.setRole(role);
        }

        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(user.getPhone());
        existingUser.setPoint(user.getPoint());

        User updatedUser = userRepository.save(existingUser);
        log.info("User with id {} updated successfully", updatedUser.getId());
        return convertUserToResUserDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = this.getUserById(id);

        this.userRepository.delete(user);
        log.info("User with id {} deleted successfully", id);

    }

    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User with email: {} not found", email);
            throw new CommonException("User with " + email + " not found");
        });
    }

    public ResultPaginationDTO getAllUser(int page, int size, String fullName, String email, String sort) {
        // custome sort (just sort one field) (fullname or updatedAt) not (fullName and
        // updatedAt)
        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            String direction = parts[1];

            sortObj = direction.equalsIgnoreCase("desc")
                    ? Sort.by(field).descending()
                    : Sort.by(field).ascending();
        }

        Page<User> users;
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        if ((fullName == null || fullName.isEmpty())
                && (email == null || email.isEmpty())) {

            users = this.userRepository.findAll(pageable);

        } else if (fullName == null || fullName.isEmpty()) {

            users = this.userRepository.findByEmailContainingIgnoreCase(email, pageable);

        } else if (email == null || email.isEmpty()) {

            users = this.userRepository.findByFullNameContainingIgnoreCase(fullName, pageable);

        } else {

            users = this.userRepository.findByFullNameAndEmailContainingIgnoreCase(fullName, email, pageable);
        }

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1); // get current page number
        meta.setPageSize(pageable.getPageSize()); // get page-size
        meta.setPages(users.getTotalPages()); // get total pages
        meta.setTotalElements(users.getTotalElements()); // get total elements in database

        resultPaginationDTO.setMeta(meta);

        List<ResponseUserDTO> userDTOs = users.getContent().stream().map(user -> this.convertUserToResUserDTO(user))
                .collect(Collectors.toList());
        resultPaginationDTO.setResults(userDTOs);
        return resultPaginationDTO;
    }

    public void updateUserRefreshToken(String email, String refreshToken) {
        User user = this.getUserByEmail(email);
        user.setRefreshToken(refreshToken);
        this.userRepository.save(user);
    }

    public User fetchUserByEmailAndRefreshToken(String email, String refrehToken) {
        return this.userRepository.findByEmailAndRefreshToken(email, refrehToken).orElseThrow(() -> {
            log.warn("User  with email and refreshToen not found");
            return new CommonException("User  with email and refreshToen not found");
        });
    }

    public Map<String, Object> createUserBulk(List<User> usersBulk) {

        List<ResponseUserDTO> success = new ArrayList<>();
        List<Map<String, String>> failed = new ArrayList<>();

        for (User u : usersBulk) {
            try {
                // check email
                if (checkExistsByEmail(u.getEmail())) {
                    Map<String, String> err = new HashMap<>();
                    err.put("email", u.getEmail());
                    err.put("error", "Email already exists");
                    failed.add(err);
                    continue;
                }

                // set role
                if (u.getRole() != null) {
                    Role role = roleService.getRoleById(u.getRole().getId());
                    u.setRole(role);
                }

                u.setPassword(passwordEncoder.encode(u.getPassword()));

                User savedUser = userRepository.save(u);
                success.add(convertUserToResUserDTO(savedUser));

            } catch (Exception e) {
                Map<String, String> err = new HashMap<>();
                err.put("email", u.getEmail());
                err.put("error", "System error");
                failed.add(err);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("total", usersBulk.size());

        return result;
    }
}
