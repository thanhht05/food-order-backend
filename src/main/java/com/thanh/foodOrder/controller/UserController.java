package com.thanh.foodorder.controller;

import com.thanh.foodorder.domain.Role;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.domain.response.ResultPaginationDTO;
import com.thanh.foodorder.dto.response.user.ResponseUserDTO;
import com.thanh.foodorder.service.RoleService;
import com.thanh.foodorder.service.UserService;
import com.thanh.foodorder.util.annotation.ApiMessage;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    // @GetMapping("/home")
    // public ResponseEntity<Void> getHomePage() {
    // User user = new User();
    // user.setEmail("ADMIN@gmail.com");
    // user.setFullName("ADMIN");
    // user.setPassword(passwordEncoder.encode("123"));

    // Role r = new Role();
    // r.setName("ADMIN");
    // this.roleService.createRole(r);

    // user.setRole(r);

    // this.userService.createUser(user);

    // return ResponseEntity.status(HttpStatus.OK).body(null);

    // }

    @PostMapping("/users")
    @ApiMessage("Create a user")
    public ResponseEntity<ResponseUserDTO> handleCreateUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(user));
    }

    @PutMapping("/users")
    @ApiMessage("Update a user")
    public ResponseEntity<ResponseUserDTO> handleUpdateUser(@RequestBody User user) {
        this.userService.updateUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.updateUser(user));

    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> handelDeleteUser(@PathVariable("id") Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by id")
    public ResponseEntity<ResponseUserDTO> handleGetUserById(@PathVariable("id") Long id) {
        User user = this.userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToResUserDTO(user));
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @RequestParam(name = "sort", required = false) String sort) {

        ResultPaginationDTO users = this.userService
                .getAllUser(page, size, fullName, email, sort);

        return ResponseEntity.ok().body(users);
    }

    @PostMapping("/users/bulk")
    @ApiMessage("Create users in bulk")
    public ResponseEntity<Map<String, Object>> createUserBulk(
            @RequestBody List<User> users) {

        Map<String, Object> result = userService.createUserBulk(users);
        return ResponseEntity.ok(result);
    }

}
