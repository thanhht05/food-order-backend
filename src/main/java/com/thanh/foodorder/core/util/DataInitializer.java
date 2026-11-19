package com.thanh.foodorder.core.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.thanh.foodorder.feature.user.domain.Role;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.RoleService;
import com.thanh.foodorder.feature.user.service.UserService;


@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initAdminAccount(UserService userService, PasswordEncoder passwordEncoder,
            RoleService roleService) {
        return args -> {
            String adminEmail = "admin@gmail.com";
            if (!userService.checkExistsByEmail(adminEmail)) {
                User user = new User();
                user.setEmail(adminEmail);
                user.setPassword("admin");
                user.setFullName("ADMIN");
                Role r = roleService.getRoleById(1);
                user.setRole(r);
                userService.createUser(user);
                System.out.println("Admin account created.");

            }

        };
    }
}
