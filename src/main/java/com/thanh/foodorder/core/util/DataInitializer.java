package com.thanh.foodorder.core.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.thanh.foodorder.feature.user.domain.Role;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.repository.RoleRepository;
import com.thanh.foodorder.feature.user.service.UserService;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initAdminAccount(UserService userService, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ADMIN");
                r.setCreatedBy("SYSTEM");
                r.setUpdatedBy("SYSTEM");
                return roleRepository.save(r);
            });

            roleRepository.findByName("USER").orElseGet(() -> {
                Role r = new Role();
                r.setName("USER");
                r.setCreatedBy("SYSTEM");
                r.setUpdatedBy("SYSTEM");
                return roleRepository.save(r);
            });

            String adminEmail = "admin@gmail.com";
            if (!userService.checkExistsByEmail(adminEmail)) {
                User user = new User();
                user.setEmail(adminEmail);
                user.setPassword("admin");
                user.setFullName("ADMIN");
                user.setRole(adminRole);
                userService.createUser(user);
                System.out.println("Admin account created.");
            }
        };
    }
}
