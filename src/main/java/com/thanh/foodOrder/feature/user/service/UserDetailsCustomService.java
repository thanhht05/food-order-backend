package com.thanh.foodorder.feature.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserDetailsCustomService implements UserDetailsService {
    private final UserService userService;

    public UserDetailsCustomService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            com.thanh.foodorder.feature.user.domain.User user = this.userService.getUserByEmail(username);
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getRole() != null && user.getRole().getName() != null && !user.getRole().getName().isBlank()) {
                String roleName = user.getRole().getName();
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleName));
            }
            return new User(user.getEmail(), user.getPassword(), authorities);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + username, e);
        }
    }

}
